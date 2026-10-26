package com.ajkumarray.margdarshak.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.util.MessageTranslator;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * For /auth/refresh and /auth/logout: reads the refresh token from
 * "Authorization: Bearer ...", rejects it unless it's a valid refresh token,
 * and exposes its userCode and jti as request attributes.
 */
@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    public static final String JTI = "refreshTokenJti";

    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        Claims claims = token != null && token.startsWith("Bearer ")
                ? jwtTokenProvider.parseRefreshToken(token.substring("Bearer ".length()))
                : null;

        if (claims == null) {
            throw new ApplicationException(
                    MessageTranslator.toLocale(ApplicationEnums.INVALID_REFRESH_TOKEN.getCode()),
                    ApplicationEnums.INVALID_REFRESH_TOKEN.getCode());
        }

        request.setAttribute(AuthContextInterceptor.USER_CODE, claims.getSubject());
        request.setAttribute(JTI, claims.getId());
        return true;
    }
}
