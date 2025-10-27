package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication

@ComponentScan(basePackages = {
	    "com.perfect.usha",
	    "com.example.demo.controller",
	    "com.example.demo.service",
	    "com.example.demo.entity",
	    "com.example.demo.config",

	    
	})


public class TestingtoolApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TestingtoolApplication.class, args);
        System.out.println("✅ Your program is running successfully!");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(TestingtoolApplication.class);
    }
}
