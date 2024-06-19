package com.dyx.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

//启用spring security webFlux支持
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    //ReactiveClientRegistrationRepository是注册到Keycloak中的客户端
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository){
        return http
                .authorizeExchange(exchange -> exchange
                        //允许未认证访问静态资源
                        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
                        // 允许未认证查看图书
                        .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
                        //其它请求都进行验证
                        .anyExchange().authenticated()
                )
                //当用户未认证而抛出异常时，返回HTTP 401，此时由Angular前端应用拦截401并显示触发认证
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))

                //认证模式一：通过登录表单启用用户认证，框架内置登录页面，内置用户user（密码见启动日志）
                //.formLogin(Customizer.withDefaults())
                //认证模式二：使用OAuth2/OpenID Connect启用用户认证，由Keycloak提供登录页面
                .oauth2Login(Customizer.withDefaults())

                // 定义退出成功时的handler
                .logout(logout-> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                // 使用cookie策略与Angular前端交换CSRF
                .csrf(csrf->csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
                .build();
    }

    //用于订阅CsrfToken反应式流的过滤器，并确保它的值能被正常提取
    @Bean
    WebFilter csrfWebFilter(){
        return (exchange, chain) -> {
           exchange.getResponse().beforeCommit(()-> Mono.defer(()->{
               Mono<CsrfToken> csrfToken=exchange.getAttribute(CsrfToken.class.getName());
               return csrfToken!=null?csrfToken.then():Mono.empty();
           }));
           return chain.filter(exchange);
        };
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository){
        var oidcLogoutSuccessHandler=new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        //重定向至应用baseUrl，由Spring动态计算获得，本地是http://localhost:9000/
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

    //将访问令牌存储到web会话中（redis），默认是存在内存中
    @Bean
    ServerOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository(){
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }
}
