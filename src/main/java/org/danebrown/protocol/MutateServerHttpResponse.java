package org.danebrown.protocol;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.FutureMono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by danebrown on 2021/4/8
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Slf4j
public class MutateServerHttpResponse extends AbstractServerHttpResponse {
    private Flux<DataBuffer> body;
    private Function<Flux<DataBuffer>, Mono<Void>> writeHandler;
    private CompletableFuture<String> response = new CompletableFuture<>();
    private Mono<String> monoResp = FutureMono.fromFuture(response);
    private Charset charset = StandardCharsets.UTF_8;

    public MutateServerHttpResponse() {
        this(new DefaultDataBufferFactory());
    }

    public MutateServerHttpResponse(DataBufferFactory dataBufferFactory) {
        super(dataBufferFactory);
//        this.body = Flux.error(new IllegalStateException("No content was written nor was setComplete() called on this response."));
        this.body = Flux.empty();
        this.writeHandler = (body) -> {
            MonoProcessor<Void> completion = MonoProcessor.create();
            completion.getClass();
            Flux fluxBody =
                    body
                            .doOnNext(new Consumer<DataBuffer>() {
                                @Override
                                public void accept(DataBuffer dataBuffer) {
                                    log.info("MutateServerHttpResponse doOnNext");
                                    String s = dataBuffer.toString(charset);
                                    response.complete(s);
                                }
                            })
                            .doOnComplete(()->{
                                log.info("MutateServerHttpResponse doOnComplete");
                                completion.onComplete();
                            })
                            .doOnError(completion::onError);
            completion.getClass();
            this.body = fluxBody.cache();
            this.body.subscribe();
            return completion;
        };
    }

    public void setWriteHandler(Function<Flux<DataBuffer>, Mono<Void>> writeHandler) {
        Assert.notNull(writeHandler, "'writeHandler' is required");
        this.body = Flux.error(new IllegalStateException("Not available with custom write handler."));
        this.writeHandler = writeHandler;
    }

    @SneakyThrows
    public <T> T getNativeResponse() {
        return (T) monoResp;
    }

    protected void applyStatusCode() {
    }

    protected void applyHeaders() {
    }

    protected void applyCookies() {
        Iterator var1 = this.getCookies().values().iterator();

        while(var1.hasNext()) {
            List<ResponseCookie> cookies = (List)var1.next();
            Iterator var3 = cookies.iterator();

            while(var3.hasNext()) {
                ResponseCookie cookie = (ResponseCookie)var3.next();
                this.getHeaders().add("Set-Cookie", cookie.toString());
            }
        }

    }

    protected Mono<Void> writeWithInternal(Publisher<? extends DataBuffer> body) {
        return (Mono)this.writeHandler.apply(Flux.from(body));
    }

    protected Mono<Void> writeAndFlushWithInternal(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return (Mono)this.writeHandler.apply(Flux.from(body).concatMap(Flux::from));
    }

    public Mono<Void> setComplete() {
        return this.doCommit(() -> {
            return Mono.defer(() -> {
                return (Mono)this.writeHandler.apply(Flux.empty());
            });
        });
    }

    public Flux<DataBuffer> getBody() {
        return this.body;
    }

    public Mono<String> getBodyAsString() {
        Charset charset = (Charset) Optional.ofNullable(this.getHeaders().getContentType()).map(MimeType::getCharset).orElse(StandardCharsets.UTF_8);
        return DataBufferUtils.join(this.getBody()).map((buffer)->{
            String s = buffer.toString(charset);
            DataBufferUtils.release(buffer);
                    return s;
        });
//        return this.getBody().reduce(this.bufferFactory().allocateBuffer(), (previous, current) -> {
//            previous.write(new DataBuffer[]{current});
//            DataBufferUtils.release(current);
//            return previous;
//        }).map((buffer) -> {
//            return bufferToString(buffer, charset);
//        });
    }

    private static String bufferToString(DataBuffer buffer, Charset charset) {
        Assert.notNull(charset, "'charset' must not be null");
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return new String(bytes, charset);
    }
}
