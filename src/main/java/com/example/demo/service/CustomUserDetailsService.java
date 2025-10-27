/*
 * package com.example.demo.service;
 * 
 * import com.example.demo.entity.UserAccount; import
 * com.example.demo.repo.UserAccountRepository; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.security.core.userdetails.*; import
 * org.springframework.security.core.authority.SimpleGrantedAuthority; import
 * org.springframework.stereotype.Service; import java.util.List;
 * 
 * @Service public class CustomUserDetailsService implements UserDetailsService
 * {
 * 
 * @Autowired private UserAccountRepository repo;
 * 
 * @Override public UserDetails loadUserByUsername(String userId) throws
 * UsernameNotFoundException { UserAccount user = repo.findByUserId(userId); if
 * (user == null) { throw new
 * UsernameNotFoundException("User not found with UserID: " + userId); }
 * 
 * return new org.springframework.security.core.userdetails.User(
 * user.getUserId(), user.getPassword(), user.getStatus() == 1, // enabled true,
 * true, true, List.of(new SimpleGrantedAuthority("ROLE_" +
 * user.getUserRole().toUpperCase())) ); } }
 */