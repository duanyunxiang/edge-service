package com.dyx.edgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver keyResolver(){
        //返回固定值，所有请求都会映射到同一个桶
        return exchange -> Mono.just("anonymous");
    }
}
