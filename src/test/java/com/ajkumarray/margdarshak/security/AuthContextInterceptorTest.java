package com.ajkumarray.margdarshak.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ajkumarray.margdarshak.enums.UserRoleEnums;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class AuthContextInterceptorTest {

    private JwtTokenProvider jwtTokenProvider;
    private AuthContextInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = JwtTokenProviderTest.provider();
        interceptor = new AuthContextInterceptor(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenExposesUserCodeAndRole() {
        MockHttpServletRequest request = requestWith(jwtTokenProvider.generateToken("MDadmin01", UserRoleEnums.ADMIN));

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(request.getAttribute(AuthContextInterceptor.USER_CODE)).isEqualTo("MDadmin01");
        assertThat(request.getAttribute(AuthContextInterceptor.USER_ROLE)).isEqualTo(UserRoleEnums.ADMIN);
    }

    @Test
    void invalidTokenSetsNothingButLetsRequestThrough() {
        String noRole = Jwts.builder().setSubject("MDuser001")
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(SignatureAlgorithm.HS256, JwtTokenProviderTest.SECRET.getBytes()).compact();
        MockHttpServletRequest request = requestWith(noRole);

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(request.getAttribute(AuthContextInterceptor.USER_CODE)).isNull();
        assertThat(request.getAttribute(AuthContextInterceptor.USER_ROLE)).isNull();
    }

    @Test
    void noTokenSetsNothingButLetsRequestThrough() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(request.getAttribute(AuthContextInterceptor.USER_CODE)).isNull();
    }

    @Test
    void filterAuthenticatesValidTokenWithoutAuthorities() throws Exception {
        MockHttpServletRequest request = requestWith(jwtTokenProvider.generateToken("MDuser001", UserRoleEnums.USER));
        new JwtAuthenticationFilter(jwtTokenProvider).doFilter(request, new MockHttpServletResponse(),
                new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo("MDuser001");
        assertThat(auth.getAuthorities()).isEmpty();
    }

    private static MockHttpServletRequest requestWith(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
