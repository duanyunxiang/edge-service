package com.dyx.edgeservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class WebEndpoints {
    // 函数式REST端点以bean形式定义
    @Bean
    public RouterFunction<ServerResponse> routerFunction(){
        //提供流畅API构建路由
        return RouterFunctions.route()
                //处理GET端点的回退响应
                .GET("/catalog-fallback",request -> ServerResponse.ok().body(Mono.just(""),String.class))
                //处理POST端点的回退响应，返503错误
                .POST("/catalog-fallback",request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build())
                //构建函数式端点
                .build();
    }
}
