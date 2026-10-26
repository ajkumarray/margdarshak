package com.ajkumarray.margdarshak.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.enums.UserRoleEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;

class RefreshTokenInterceptorTest {

    private final JwtTokenProvider jwtTokenProvider = JwtTokenProviderTest.provider();
    private final RefreshTokenInterceptor interceptor = new RefreshTokenInterceptor(jwtTokenProvider);

    @Test
    void validRefreshTokenExposesUserCodeAndJti() {
        String token = jwtTokenProvider.generateRefreshToken("MDuser001", "jti-1",
                new Date(System.currentTimeMillis() + 3_600_000));
        MockHttpServletRequest request = requestWith("Bearer " + token);

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(request.getAttribute(AuthContextInterceptor.USER_CODE)).isEqualTo("MDuser001");
        assertThat(request.getAttribute(RefreshTokenInterceptor.JTI)).isEqualTo("jti-1");
    }

    @Test
    void missingOrInvalidRefreshTokenIsRejected() {
        String accessToken = jwtTokenProvider.generateToken("MDuser001", UserRoleEnums.USER);

        assertRejected(new MockHttpServletRequest());
        assertRejected(requestWith("Basic abc"));
        assertRejected(requestWith("Bearer not-a-jwt"));
        assertRejected(requestWith("Bearer " + accessToken));
    }

    private void assertRejected(MockHttpServletRequest request) {
        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ApplicationException.class)
                .extracting(e -> ((ApplicationException) e).getErrorCode())
                .isEqualTo(ApplicationEnums.INVALID_REFRESH_TOKEN.getCode());
    }

    private static MockHttpServletRequest requestWith(String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", authorization);
        return request;
    }
}
