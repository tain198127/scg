//package org.danebrown.route;
//
//import org.danebrown.scgfilter.PostMockFilter;
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Created by danebrown on 2021/6/4
// * mail: tain198127@163.com
// *
// * @author danebrown
// */
//@Configuration
//public class MockRoute {
//    @Bean
//    public RouteLocator routeLocator(RouteLocatorBuilder builder){
//        return builder.routes().route(r ->
//                r.path("/thread/**")
//                        //转发路由
//                        .uri("http://127.0.0.1:8080/hello/")
//                        //注册自定义过滤器
//                        .filters(new PostMockFilter())
//                        //给定id
//                        .id("thread"))
//                .build();
//
//    }
//}
