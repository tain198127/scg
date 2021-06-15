package org.danebrown.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.danebrown.exception.SentinelExceptionHandler;
import org.danebrown.mode.RequestMod;
import org.danebrown.mode.ResponseMod;
import org.danebrown.scgfilter.PreSentinelFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.danebrown.config.SentinelConst.OBJECT_KEY;

/**
 * Created by danebrown on 2021/2/24
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Configuration
@Slf4j
public class SentinelFilterConfiguration {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelFilterConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                       ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }
    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter() {
        return new PreSentinelFilter();
    }

//    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter1() {
        return new SentinelGatewayFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelExceptionHandler sentinelGatewayBlockExceptionHandler() {
        // Register the block exception handler for Spring Cloud Gateway.
        return new SentinelExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

//    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler1() {
        // Register the block exception handler for Spring Cloud Gateway.
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }


    @PostConstruct
    public void doInit() {
        initCustomizedApis();
        initGatewayRules();
        loadDegradeRules();
        loadFlowRules();
        registerCbk();
    }
    private void registerCbk(){
//        GatewayCallbackManager.setRequestOriginParser((se)->{
//            RequestMod mod = (RequestMod) se.getAttributes().get(OBJECT_KEY);
//            mod = Optional.of(mod).orElse(new RequestMod());
//            return ((RequestMod) se.getAttributes().get(OBJECT_KEY)).getOrigin();
//        });
        /**
         * 注册一个拒绝处理方法，用于处理超时拦截、熔断拦截、限流等等
         */
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            /**
             * 处理降级
             * @param exchange
             * @param t
             * @return
             */
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                log.info("FluxMockHandler");

                //        ResponseMod responseMod = (ResponseMod) exchange.getAttributes().get(OBJECT_KEY);
                //被限流
                ResponseMod responseMod = new ResponseMod();
                responseMod.setErrorCode("ERROR");
                responseMod.setBody(t.getMessage());
                responseMod.setCode(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.toString());
                if(t instanceof FlowException){
                    log.info("FlowException");

                    FlowException flowException = (FlowException) t;
                    responseMod.setBody(JSON.toJSONString(flowException.getRule()));
                    responseMod.setCode(HttpStatus.TOO_MANY_REQUESTS.toString());
                }
                else if(t instanceof DegradeException){
                    log.info("DegradeException");
                    DegradeException degradeException = (DegradeException) t;
                    responseMod.setBody(JSON.toJSONString(degradeException.getRule()));
                    responseMod.setCode(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.toString());
                }

//                DataBuffer byteBuffer = exchange.getResponse().bufferFactory()
//                        .wrap(JSON.toJSONBytes(responseMod));

                String json = JSON.toJSONString(responseMod);

//                exchange.getResponse().writeWith(Mono.just(byteBuffer));
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCEPT_CHARSET,"utf-8")
                        .body(BodyInserters.fromValue(json))
                        ;

            }
        });
        /**
         * 触发熔断时的事件
         */
        EventObserverRegistry.getInstance().addStateChangeObserver("logging",
                (prevState, newState, rule, snapshotValue) -> {
                    if (newState == CircuitBreaker.State.OPEN) {
                        // 变换至 OPEN state 时会携带触发时的值
                        log.error("{} -> OPEN at {}, snapshotValue={%.2f}",
                                prevState.name(),
                                TimeUtil.currentTimeMillis(), snapshotValue);
                    } else {
                        log.error("{} -> {} at {}", prevState.name(),
                                newState.name(),
                                TimeUtil.currentTimeMillis());
                    }
                });
        /**
         * 获取请求origin的一个解析器
         */
//        GatewayCallbackManager.setRequestOriginParser((req)->{
//            RequestMod requestMod = (RequestMod) req.getAttributes().get(OBJECT_KEY);
//            return requestMod.getOrigin();
//        });

        StatisticSlotCallbackRegistry.addEntryCallback("block", new ProcessorSlotEntryCallback<DefaultNode>() {
            @Override
            public void onPass(Context context, ResourceWrapper resourceWrapper, DefaultNode param, int count, Object... args) throws Exception {
                log.info("onPass context:{};resource:{};param:{};count:{}",
                        context,
                        resourceWrapper,
                        param,count);
            }

            @Override
            public void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, DefaultNode param, int count, Object... args) {
                log.info("onBlocked exception:{};context:{};resource:{};" +
                                "param:{};count:{}",
                        ex,
                        context,
                        resourceWrapper,
                        param,count);
            }
        });
        StatisticSlotCallbackRegistry.addExitCallback("block", new ProcessorSlotExitCallback() {
            @Override
            public void onExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
                log.info("onExit context:{};resource:{};param:{};count:{}",
                        context,
                        resourceWrapper,
                        count);
            }
        });
    }

    private void loadDegradeRules(){
        List<DegradeRule> degradeRules = new ArrayList<>();
        DegradeRule mixRule = new DegradeRule("mix")
                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                // Set ratio threshold to 50%.
                .setCount(0.1d)
                .setStatIntervalMs(30000)
                .setMinRequestAmount(50)
                // Retry timeout (in second)

                .setTimeWindow(10)
                ;


        DegradeRule blockrule = new DegradeRule("block")
                .setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType())
                // Set ratio threshold to 50%.
                .setCount(2)
                .setStatIntervalMs(30000)
                .setMinRequestAmount(1)

                // Retry timeout (in second)
                .setTimeWindow(10);
        degradeRules.add(blockrule);
        degradeRules.add(mixRule);
        DegradeRuleManager.loadRules(degradeRules);
    }



    private void loadFlowRules(){
        //1qps
        List<FlowRule> rules = new ArrayList<FlowRule>();
        //使用线程限流
        FlowRule rule1 = new FlowRule();
        rule1.setResource("thread");
        // set limit concurrent thread for 'methodA' to 20
        rule1.setCount(2);
        rule1.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule1.setLimitApp("default");
        rule1.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(rule1);

        //QPS限流
        FlowRule rule2 = new FlowRule();
        rule2.setResource("testApi");
        rule2.setCount(1);
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setLimitApp("default");
        rules.add(rule2);

        //混合
        FlowRule rule3 = new FlowRule();
        rule3.setResource("mix");
        rule3.setCount(5);
        rule3.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule3.setLimitApp("default");
        rules.add(rule3);

//        for (int i=0;i < 1000; i++){
//            FlowRule ruleTest = new FlowRule();
//            ruleTest.setResource("mix"+i);
//            ruleTest.setCount(5);
//            ruleTest.setGrade(RuleConstant.FLOW_GRADE_QPS);
//            ruleTest.setLimitApp("default");
//            rules.add(ruleTest);
//        }

        FlowRuleManager.loadRules(rules);
    }
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition api1 = new ApiDefinition("testApi")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/ahas"));
                    add(new ApiPathPredicateItem().setPattern("/seq/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        ApiDefinition api2 = new ApiDefinition("another_customized_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(api1);
//        definitions.add(api2);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("testApi")
                .setCount(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setIntervalSec(1)
        );
        rules.add(new GatewayFlowRule("aliyun_route")
                .setCount(2)
                .setIntervalSec(2)
                .setBurst(2)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                )
        );
        rules.add(new GatewayFlowRule("httpbin_route")
                .setCount(10)
                .setIntervalSec(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeoutMs(600)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                        .setFieldName("X-Sentinel-Flag")
                )
        );
        rules.add(new GatewayFlowRule("httpbin_route")
                .setCount(1)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pa")
                )
        );
        rules.add(new GatewayFlowRule("httpbin_route")
                .setCount(2)
                .setIntervalSec(30)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("type")
                        .setPattern("warn")
                        .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_CONTAINS)
                )
        );

        /*
        rules.add(new GatewayFlowRule("some_customized_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(5)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pn")
                )
        );
        */
        GatewayRuleManager.getRules().clear();
        GatewayRuleManager.loadRules(rules);
    }
}
