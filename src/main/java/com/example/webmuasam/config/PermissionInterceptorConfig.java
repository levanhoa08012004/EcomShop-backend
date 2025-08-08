package com.example.webmuasam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfig implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor permissionInterceptor() {
        return new PermissionInterceptor();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {"/","/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/register"
                ,"/api/v1/products","/api/v1/products/**",
                "/api/v1/categories","/api/v1/categories/**",
                "/api/v1/users",
                "/api/v1/users/**",
                "/api/v1/payments/vnpay_ipn",
                "/order/*/status",

        };
        registry.addInterceptor(permissionInterceptor()).excludePathPatterns(whiteList);
    }
}
