package org.danebrown.praser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

/**
 * Created by danebrown on 2021/3/6
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Component
@Slf4j
public class GatewayResourceParser implements SentinelResourceParser<ServerWebExchange> {
    public String generateByObject(ServerWebExchange exchange){
        return null;
    }

    @Override
    public String parse(ServerWebExchange exchange) {
        String query =
                Optional.ofNullable(exchange.getRequest().getQueryParams().getFirst("a")
        ).orElse("");
        log.info("parse:{}",query);
        return query;
//        return (String) Optional.ofNullable(exchange.getAttribute("aaa")).orElse("1");
    }
}
