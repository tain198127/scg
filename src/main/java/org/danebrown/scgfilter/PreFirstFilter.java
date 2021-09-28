package org.danebrown.scgfilter;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.danebrown.mode.RequestMod;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.ReactorNetty;

import static org.danebrown.config.SentinelConst.OBJECT_KEY;

/**
 * Created by danebrown on 2021/2/25
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Slf4j
@Service
public class PreFirstFilter implements GlobalFilter, Ordered {
    private final int order;
    private static String USE_PUBLISH_ON="USE_PUBLISH_ON";
    private boolean isPublishOn = Boolean.parseBoolean(System.getProperty(
            USE_PUBLISH_ON,
            "false"));

    Gson gson = new Gson();

    public PreFirstFilter() {
        this(-1);
    }

    public PreFirstFilter(int order) {
        this.order = order;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


         Mono<String> ret = Mono.fromCallable(() -> {
            ThreadLocalContext.idx.set("123");//放置上下文
            RequestMod testMod = new RequestMod();
            testMod.getHeader().put("channel", "ap");
            testMod.getBody().put("money", 50);

            log.info("{}", testMod);

            exchange.getAttributes().put(OBJECT_KEY, testMod);
            log.info("PreFirstFilter fromRunnable");
            return "ok";
            //这里放最最最开始那个_filter部分的逻辑
        });
                if(isPublishOn){
                    ret = ret.publishOn(Schedulers.boundedElastic());
                }

        return ret.flatMap((a) -> chain.filter(exchange));


    }

    @Override
    public int getOrder() {
        return order;
    }
}
