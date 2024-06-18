package com.dyx.edgeservice.user;

import java.util.List;

import com.dyx.edgeservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(UserControllerTests.class)
// 导入应用的安全配置
@Import(SecurityConfig.class)
public class UserControllerTests {
    @Autowired
    WebTestClient webClient;
    //跳过与Keycloak的交互
    @MockBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenNoAuthenticatedThen401(){
        //验证用户未验证时，返回401
        webClient.get().uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    //@Test
    void whenAuthenticatedThenReturnUser(){
        var expectedUser=new User("jon.snow","Jon","Snow", List.of("employee","customer"));

        webClient
                //基于OIDC和预期用户mock认证上下文
                .mutateWith(configureMockOidcLogin(expectedUser))
                .get().uri("/user")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(User.class)
                .value(user -> assertThat(user).isEqualTo(expectedUser));
    }

    private SecurityMockServerConfigurers.OidcLoginMutator configureMockOidcLogin(User expectedUser){
        return SecurityMockServerConfigurers.mockOidcLogin().idToken(builder -> {
            //构建mock ID令牌
            builder.claim(StandardClaimNames.PREFERRED_USERNAME,expectedUser.username());
            builder.claim(StandardClaimNames.GIVEN_NAME,expectedUser.firstName());
            builder.claim(StandardClaimNames.FAMILY_NAME,expectedUser.lastName());
        });
    }
}
