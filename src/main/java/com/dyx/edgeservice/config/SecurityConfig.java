package com.dyx.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

//启用spring security webFlux支持
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    //ReactiveClientRegistrationRepository是注册到Keycloak中的客户端
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository){
        return http.authorizeExchange(exchange->
                        //所有请求都进行验证
                        exchange.anyExchange().authenticated())
                //认证模式一：通过登录表单启用用户认证，框架内置登录页面，内置用户user（密码见启动日志）
                //.formLogin(Customizer.withDefaults())

                //认证模式二：使用OAuth2/OpenID Connect启用用户认证，由Keycloak提供登录页面
                .oauth2Login(Customizer.withDefaults())
                // 定义退出成功时的handler
                .logout(logout-> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                .build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository){
        var oidcLogoutSuccessHandler=new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        //重定向至应用baseUrl，由Spring动态计算获得，本地是http://localhost:9000/
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }
}
