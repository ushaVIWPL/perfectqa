/*
 * package com.example.demo.config;
 * 
 * import com.example.demo.service.CustomUserDetailsService; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.security.authentication.AuthenticationManager; import
 * org.springframework.security.config.annotation.authentication.builders.
 * AuthenticationManagerBuilder; import
 * org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 * import org.springframework.security.crypto.password.PasswordEncoder; import
 * org.springframework.security.web.SecurityFilterChain; import
 * org.springframework.beans.factory.annotation.Autowired;
 * 
 * @Configuration public class SecurityConfig {
 * 
 * @Autowired private CustomUserDetailsService userDetailsService;
 * 
 * @Bean public PasswordEncoder passwordEncoder() { return new
 * BCryptPasswordEncoder(); }
 * 
 * @Bean public AuthenticationManager authManager(HttpSecurity http,
 * PasswordEncoder encoder) throws Exception { AuthenticationManagerBuilder auth
 * = http.getSharedObject(AuthenticationManagerBuilder.class);
 * auth.userDetailsService(userDetailsService).passwordEncoder(encoder); return
 * auth.build(); }
 * 
 * @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http)
 * throws Exception { http .csrf(csrf -> csrf.disable()) // Disable only if
 * using form login safely .authorizeHttpRequests(auth -> auth
 * .requestMatchers("/register", "/create", "/forgot-password",
 * "/reset-password", "/css/**", "/js/**").permitAll()
 * .anyRequest().authenticated() ) .formLogin(form -> form .loginPage("/login")
 * // your Thymeleaf login page .loginProcessingUrl("/perform_login") // form
 * action .defaultSuccessUrl("/dashboard", true) // redirect after success
 * .failureUrl("/login?error=true") // redirect on failure .permitAll() )
 * .logout(logout -> logout .logoutUrl("/logout")
 * .logoutSuccessUrl("/login?logout=true") .invalidateHttpSession(true)
 * .clearAuthentication(true) .permitAll() ); return http.build(); } }
 */