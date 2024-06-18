package com.dyx.edgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver keyResolver(){
        return exchange -> exchange.getPrincipal()
                //使用认证用户名进行限流
                .map(Principal::getName)
                //没有用户时，用anonymous作为限流默认键
                .defaultIfEmpty("anonymous");
    }
}
