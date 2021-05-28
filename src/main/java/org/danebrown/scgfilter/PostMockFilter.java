package org.danebrown.scgfilter;

import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.netty.util.concurrent.FastThreadLocalThread;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.danebrown.mode.ResponseMod;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by danebrown on 2021/2/24
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Service
@Slf4j
public class PostMockFilter implements GlobalFilter, Ordered {


    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("PreMockFilter");
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        List<String> header = Lists.newArrayList();
        header.add("application/json");
        response.getHeaders().put(HttpHeaders.CONTENT_TYPE, header);
        Mono<ResponseMod> mono = Mono.fromCallable(()->{
            ResponseMod responseMod = new ResponseMod();
            responseMod.setBody("123");
            responseMod.setCode("456");
            log.info("fromCallable");
            return responseMod;
        });

        if (exchange.getRequest().getURI().getPath().contains("block")) {
            mono.doOnNext((mono1) -> {
                log.error("block");

                mono1.setCode("block");


                Throwable t = new RuntimeException("block");

                DataBuffer byteBuffer = exchange.getResponse().bufferFactory().wrap(JSON.toJSONBytes(mono1));

                response.setStatusCode(HttpStatus.OK);
                exchange.getAttributes().put("response", mono1);
                response.writeWith(Mono.just(byteBuffer));
            });
        } else if (exchange.getRequest().getURI().getPath().contains("mix")) {
            mono.doOnNext((mono1) -> {
                log.info("mix");
                mono1.setCode("mix");
                if (ThreadLocalRandom.current().nextBoolean()) {
                    log.error("mix-->出错");
                    Throwable t = new DegradeException("block");
                    Tracer.trace(t);
                }
            });

        } else if (exchange.getRequest().getURI().getPath().contains("thread")) {
            mono = mono
//                    .publishOn(Schedulers.elastic())
                    .doOnNext(responseMod1 -> {
                        try {
                            FastThreadLocalThread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        log.info("ThreadLocalContext:{}",
                                ThreadLocalContext.idx.get());
                        log.info("thread");
                        responseMod1.setCode("thread");
                    })
                    .filter(mm->{
                        log.info("第二个publish");
                        return true;
                    })

            ;
        }

        return mono.flatMap(res -> {
            DataBuffer byteBuffer = exchange.getResponse().bufferFactory().wrap(JSON.toJSONBytes(res));
            response.setStatusCode(HttpStatus.OK);
            log.info("flatMap");
            return response.writeWith(Mono.just(byteBuffer));
        });


    }

    @Override
    public int getOrder() {
        return 3000;
    }
}
