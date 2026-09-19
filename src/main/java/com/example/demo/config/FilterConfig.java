package com.example.demo.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthFilter());
        registrationBean.addUrlPatterns("/Menu","/register", "/admin", "/business-manager", "/qatestermenu", "/dashboard/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
