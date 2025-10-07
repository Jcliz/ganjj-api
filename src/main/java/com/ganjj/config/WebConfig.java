package com.ganjj.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<AdminAuthorizationFilter> adminAuthorizationFilter(AdminAuthorizationFilter filter) {
        FilterRegistrationBean<AdminAuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(
            "/api/users/*",
            "/api/products/*",
            "/api/categories/*"
        );
        return registrationBean;
    }
}