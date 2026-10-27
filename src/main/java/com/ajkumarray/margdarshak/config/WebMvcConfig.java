package com.ajkumarray.margdarshak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import com.ajkumarray.margdarshak.security.AdminInterceptor;
import com.ajkumarray.margdarshak.security.AuthContextInterceptor;
import com.ajkumarray.margdarshak.security.RefreshTokenInterceptor;

import lombok.AllArgsConstructor;

@Component
@Configuration
@EnableWebMvc
@AllArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthContextInterceptor authContextInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final RefreshTokenInterceptor refreshTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addInterceptor(authContextInterceptor);
        /*
         * Must come after authContextInterceptor, which sets the role it checks.
         */
        registry.addInterceptor(adminInterceptor).addPathPatterns("/api/v1/site/**");
        registry.addInterceptor(refreshTokenInterceptor).addPathPatterns("/api/v1/auth/refresh", "/api/v1/auth/logout");
    }
}
