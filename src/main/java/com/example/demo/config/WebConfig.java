package com.example.demo.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        // Create the uploads directory if it doesn't exist
        try {
            Files.createDirectories(uploadPath);
        } catch (Exception e) {
            System.err.println("WARNING: Could not create uploads directory: " + uploadPath);
        }
        
        String uploadLocation = uploadPath.toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }
        
        System.out.println(">>> Upload resource location mapped to: " + uploadLocation);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/", "file:./uploads/", uploadLocation);
    }

}
