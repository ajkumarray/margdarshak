package com.ajkumarray.margdarshak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MargdarshakApplication {
    public static void main(String[] args) {
        SpringApplication.run(MargdarshakApplication.class, args);
    }
} 