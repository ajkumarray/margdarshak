package com.ajkumarray.margdarshak.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Exposes the caller's userCode and role (from a valid token) as request
 * attributes, readable in controllers via {@code @RequestAttribute}. Never
 * blocks a request; role checks are done where they're needed.
 *
 * Request attributes rather than headers: a header could be sent by the client
 * itself, an attribute can only be set server-side after the token is verified.
 */
@Component
public class AuthContextInterceptor implements HandlerInterceptor {

    public static final String USER_CODE = "userCode";
    public static final String USER_ROLE = "role";

    private final JwtTokenProvider jwtTokenProvider;

    public AuthContextInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ") && jwtTokenProvider.validateToken(token)) {
            request.setAttribute(USER_CODE, jwtTokenProvider.getUserCodeFromToken(token));
            request.setAttribute(USER_ROLE, jwtTokenProvider.getRoleFromToken(token));
        }

        return true;
    }
}
