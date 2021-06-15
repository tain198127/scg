package org.danebrown.scgfilter;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.sc.api.GatewayApiMatcherManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.api.matcher.WebExchangeApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.reactor.ContextConfig;
import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.danebrown.mode.RequestMod;
import org.danebrown.praser.CustomHeaderExchangeItemParser;
import org.danebrown.praser.SentinelResourceParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.danebrown.config.SentinelConst.OBJECT_KEY;

/**
 * Created by danebrown on 2021/2/25
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Slf4j
public class PreSentinelFilter implements GlobalFilter, Ordered {

    private final int order;

    public PreSentinelFilter() {
        this(2000);
    }

    public PreSentinelFilter(int order) {
        this.order = order;
    }

    private final GatewayParamParser<ServerWebExchange> paramParser = new GatewayParamParser<>(
            new CustomHeaderExchangeItemParser());

    @Autowired
    SentinelResourceParser<ServerWebExchange> resourceParser;
    private String generateSentinelKey(ServerWebExchange exchange,
                                  GatewayFilterChain chain){
        RequestMod requestMod =  exchange.getAttribute(OBJECT_KEY);
        return "";
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        log.info("sentinel");
        Mono<Void> asyncResult = chain.filter(exchange);
        asyncResult.doOnNext(new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {
                log.info("PreSentinelFilter  doOnNext");
            }
        })
        ;
        String resourceKey = resourceParser.parse(exchange);
        if(!Strings.isNullOrEmpty(resourceKey)){
            Object[] params = paramParser.parseParameterFor(resourceKey, exchange,
                    r -> r.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME);
            String origin = Optional.ofNullable(GatewayCallbackManager.getRequestOriginParser())
                    .map(f -> f.apply(exchange))
                    .orElse("default");
            asyncResult = asyncResult.transform(
                    new SentinelReactorTransformer<>(new EntryConfig(resourceKey, ResourceTypeConstants.COMMON_API_GATEWAY,
                            EntryType.IN, 1, params, new ContextConfig(contextName(resourceKey), origin)))
            );
        }

        if (route != null) {

            String routeId = route.getId();
            Object[] params = paramParser.parseParameterFor(routeId, exchange,
                    r -> r.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID);
            String origin = Optional.ofNullable(GatewayCallbackManager.getRequestOriginParser())
                    .map(f -> f.apply(exchange))
                    .orElse("");
            asyncResult = asyncResult.transform(
                    new SentinelReactorTransformer<>(new EntryConfig(routeId,
                            ResourceTypeConstants.COMMON,
                            EntryType.IN, 1, params, new ContextConfig(contextName(routeId), origin)))
            );
        }

        Set<String> matchingApis = pickMatchingApiDefinitions(exchange);
        for (String apiName : matchingApis) {
            Object[] params = paramParser.parseParameterFor(apiName, exchange,
                    r -> r.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME);
            asyncResult = asyncResult.transform(
                    new SentinelReactorTransformer<>(new EntryConfig(apiName,
                            ResourceTypeConstants.COMMON_API_GATEWAY,
                            EntryType.IN, 1, params))
            );
        }

        return asyncResult;
    }

    private String contextName(String route) {
        return SentinelGatewayConstants.GATEWAY_CONTEXT_ROUTE_PREFIX + route;
    }

    Set<String> pickMatchingApiDefinitions(ServerWebExchange exchange) {
        return GatewayApiMatcherManager.getApiMatcherMap().values()
                .stream()
                .filter(m -> m.test(exchange))
                .map(WebExchangeApiMatcher::getApiName)
                .collect(Collectors.toSet());
    }

    @Override
    public int getOrder() {
        return order;
    }
}
