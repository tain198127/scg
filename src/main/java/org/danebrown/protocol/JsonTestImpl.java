package org.danebrown.protocol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.adapter.HttpWebHandlerAdapter;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Created by danebrown on 2021/4/7
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@RestController
@Slf4j
public class JsonTestImpl {
    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    @Autowired
    private ApplicationContext ctx;

    private static Charset charset = StandardCharsets.UTF_8;

    @PreDestroy
    public void preDestory(){
        log.error("preDestory");
    }
    @PostConstruct
    public void afterDestory(){

    }

    @PostMapping(value = "/dubbotest")
    public Mono<String> test(String json) {
        String jsonVar = "{\n" + "\t\"header\":\n" + "\t\t{\n" + "\t\t\t" +
                "\"channel\":\"ap\"\n" + "\t\t},\n" + "\t\"body\":\n" + "\t\t{\n" + "\t\t\t\"money\":50\n" + "\t\t}\n" + "}";
        HttpWebHandlerAdapter adapter =
                ctx.getBean(HttpWebHandlerAdapter.class);
        MutateServerWebExchange exchange =
                MutateServerWebExchange.from(MutatedServerHttpRequest.post(
                        "/thread","")
                        .header("Content-Type","application/json")
                        .body(jsonVar));
        exchange.getAttributes().put(CACHE_REQUEST_BODY_OBJECT_KEY,jsonVar);
        Mono<Void> voidMono = adapter.handle(exchange);
        voidMono.subscribe();

        Mono<String> rest =
                voidMono
            .as(new Function<Mono<Void>, Mono<String>>() {
            @Override
            public Mono<String> apply(Mono<Void> voidMono) {
                log.info("dubbo 的 apply");
                DataBuffer buffer = exchange.getResponse().getNativeResponse();
                String s = buffer.toString(charset);
                return Mono.just(s);
            }
        }).flatMap(r->{
            return Mono.just(r);
                });
        return rest;


    }
}
