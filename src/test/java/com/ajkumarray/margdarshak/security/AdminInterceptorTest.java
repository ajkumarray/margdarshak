package com.ajkumarray.margdarshak.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.enums.UserRoleEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;

class AdminInterceptorTest {

    private final AdminInterceptor interceptor = new AdminInterceptor();

    @Test
    void adminIsLetThrough() {
        assertThat(interceptor.preHandle(requestWithRole(UserRoleEnums.ADMIN), new MockHttpServletResponse(),
                new Object())).isTrue();
    }

    @Test
    void userIsDenied() {
        assertDenied(requestWithRole(UserRoleEnums.USER));
    }

    @Test
    void anonymousIsDenied() {
        assertDenied(new MockHttpServletRequest());
    }

    private void assertDenied(MockHttpServletRequest request) {
        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ApplicationException.class)
                .extracting(e -> ((ApplicationException) e).getErrorCode())
                .isEqualTo(ApplicationEnums.ACCESS_DENIED.getCode());
    }

    private static MockHttpServletRequest requestWithRole(UserRoleEnums role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AuthContextInterceptor.USER_ROLE, role);
        return request;
    }
}
