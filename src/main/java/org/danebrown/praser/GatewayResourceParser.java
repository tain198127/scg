package org.danebrown.praser;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * Created by danebrown on 2021/3/6
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Component
public class GatewayResourceParser implements SentinelResourceParser<ServerWebExchange> {
    public String generateByObject(ServerWebExchange exchange){
        return null;
    }

    @Override
    public String parse(ServerWebExchange exchange) {
        return null;
    }
}
