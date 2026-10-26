package com.ajkumarray.margdarshak.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.enums.UserRoleEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.util.MessageTranslator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lets a request through only if the caller is an ADMIN. Relies on
 * {@link AuthContextInterceptor} having set the role attribute, so it must be
 * registered after it.
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getAttribute(AuthContextInterceptor.USER_ROLE) != UserRoleEnums.ADMIN) {
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.ACCESS_DENIED.getCode()),
                    ApplicationEnums.ACCESS_DENIED.getCode());
        }
        return true;
    }
}
