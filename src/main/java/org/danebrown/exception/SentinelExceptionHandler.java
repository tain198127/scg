package org.danebrown.exception;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.danebrown.mode.ResponseMod;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by danebrown on 2021/3/2
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Slf4j
public class SentinelExceptionHandler implements WebExceptionHandler {

    private List<ViewResolver> viewResolvers;
    private List<HttpMessageWriter<?>> messageWriters;
    private final Supplier<ServerResponse.Context> contextSupplier = () -> new ServerResponse.Context() {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return viewResolvers;
        }
    };


    public SentinelExceptionHandler(List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolvers;
        this.messageWriters = serverCodecConfigurer.getWriters();
    }

    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange) {

        return response.writeTo(exchange, contextSupplier.get());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ResponseMod error = new ResponseMod();
        log.info("SentinelExceptionHandler");
        error.setCode("ERROR");
        error.setBody("ERROR");
        //限流
//        if(FlowException.isBlockException(ex)){
//            log.error("请求:[{}]被限流",exchange.getRequest().getPath(),ex);
//        }
        //限流
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        // This exception handler only handles rejection by Sentinel.
        //熔断
        if (!BlockException.isBlockException(ex)) {
            if(exchange.getAttributes().containsKey("response")){
                ResponseMod json = exchange.getAttribute("response");

                json.setCode("BOLCK");
                DataBuffer dataBuffer =
                        exchange.getResponse().bufferFactory().wrap(JSON.toJSONBytes(json));

                return Mono.just(error).flatMap(res->{
                    return exchange.getResponse().writeWith(Flux.just(dataBuffer)
                    );
                    //            return chain.filter(exchange);
                });
            }
            else{
                return Mono.error(ex);
            }

//
        }
        return handleBlockedRequest(exchange, ex).flatMap(response -> writeResponse(response, exchange));
    }

    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }

}
