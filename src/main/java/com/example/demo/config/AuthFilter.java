package com.example.demo.config;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/Menu", "/admin", "/business-manager", "/qatestermenu", "/dashboard/*", "/api/*"}) // protect these pages
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // Check if session has been flagged for concurrent login force logout
        if (session != null && session.getAttribute("forceLogout") != null) {
            session.invalidate();
            String uri = req.getRequestURI();
            String accept = req.getHeader("Accept");
            if (uri.contains("/api/") || (accept != null && accept.contains("application/json"))) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"forceLogout\": true, \"message\": \"Different login detected. System logged you out.\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/loginform?concurrent=true");
            }
            return;
        }

        // check if logged in
        boolean loggedIn = (session != null && session.getAttribute("userId") != null);

        // check if it's a login page, OTP verification, or static resource
        String uri = req.getRequestURI();
        boolean isLoginRequest = uri.endsWith("/loginform") || uri.endsWith("/login") 
                                || uri.endsWith("/verify-otp") || uri.endsWith("/resend-otp");

        if (loggedIn || isLoginRequest) {
            chain.doFilter(request, response); // continue normally
        } else {
            res.sendRedirect(req.getContextPath() + "/loginform");
        }
    }
}
