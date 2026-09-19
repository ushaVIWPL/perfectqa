/*
 * package com.example.demo.config;
 * 
 * import org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import org.springframework.security.web.SecurityFilterChain;
 * 
 * @Configuration public class SecurityConfig {
 * 
 * @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http)
 * throws Exception { http .authorizeHttpRequests(auth -> auth // Allow login
 * and static resources without authentication .requestMatchers("/loginform",
 * "/css/**", "/js/**", "/images/**").permitAll() // Everything else requires
 * login .anyRequest().authenticated() ) .formLogin(form -> form
 * .loginPage("/loginform") // Your login page .loginProcessingUrl("/login") //
 * POST URL for login form .defaultSuccessUrl("/Menu", true) // Redirect after
 * successful login .permitAll() ) .logout(logout -> logout
 * .logoutSuccessUrl("/loginform?logout") // Redirect after logout .permitAll()
 * ) .csrf(csrf -> csrf.disable()); // optional if you’re not using CSRF tokens
 * in form
 * 
 * return http.build(); } }
 */