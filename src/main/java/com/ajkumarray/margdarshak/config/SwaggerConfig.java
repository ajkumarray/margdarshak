package com.ajkumarray.margdarshak.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

        @Bean
        public OpenAPI openAPI() {

                return new OpenAPI().components(new Components())
                                .info(new Info().title("Margdarshak").description("URL Shortener API server service")
                                                .termsOfService("terms")
                                                .contact(new Contact().email("mail.ajkumarray@gamil.com"))
                                                .license(new License().name("MIT")).version("1.0.0"));
        }
}