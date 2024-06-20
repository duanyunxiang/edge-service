package com.dyx.edgeservice.security;

import com.dyx.edgeservice.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
public class UserController {
    @GetMapping("user")
    //使用自动注入获取Principal
    public Mono<User> getUser(@AuthenticationPrincipal OidcUser oidcUser){
        var user=new User(
                //用户名
                oidcUser.getPreferredUsername(),
                oidcUser.getGivenName(),
                oidcUser.getFamilyName(),
                //获取roles claim
                oidcUser.getClaimAsStringList("roles")
        );
        log.info("获取当前认证用户，user：{}",oidcUser);
        //包装为反应式响应
        return Mono.just(user);
    }

    public Mono<User> getUser2(){
        //获取当前认证用户的SecurityContext
        return ReactiveSecurityContextHolder.getContext()
                //获取Authentication
                .map(SecurityContext::getAuthentication)
                //获取Principal（因为使用的是OIDC，所以类型为OidcUser）
                .map(authentication -> (OidcUser) authentication.getPrincipal())
                .map(oidcUser -> new User(
                        //用户名
                        oidcUser.getPreferredUsername(),
                        oidcUser.getGivenName(),
                        oidcUser.getFamilyName(),
                        List.of("employee","customer"))
                );
    }
}
