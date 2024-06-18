package com.dyx.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

//启用spring security webFlux支持
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http){
        return http.authorizeExchange(exchange->
                        //所有请求都进行验证
                        exchange.anyExchange().authenticated())
                //认证模式一：通过登录表单启用用户认证，框架内置登录页面，内置用户user（密码见启动日志）
                //.formLogin(Customizer.withDefaults())

                //认证模式二：使用OAuth2/OpenID Connect启用用户认证，由Keycloak提供登录页面
                .oauth2Login(Customizer.withDefaults())
                .build();
    }
}
