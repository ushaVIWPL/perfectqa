package com.example.demo.util;

import jakarta.servlet.http.HttpSession;

public class RoleAccessUtil {
    
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_BUSINESS_MANAGER = "BUSINESS_MANAGER";
    public static final String ROLE_TESTER = "TESTER";
    
    /**
     * Get current user role from session
     */
    public static String getCurrentRole(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("role");
    }
    
    /**
     * Get current user company code from session
     */
    public static String getCurrentCompanyCode(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("companyCode");
    }
    
    /**
     * Check if current user is Admin
     */
    public static boolean isAdmin(HttpSession session) {
        return ROLE_ADMIN.equals(getCurrentRole(session));
    }
    
    /**
     * Check if current user is Business Manager
     */
    public static boolean isBusinessManager(HttpSession session) {
        return ROLE_BUSINESS_MANAGER.equals(getCurrentRole(session));
    }
    
    /**
     * Check if current user is Tester
     */
    public static boolean isTester(HttpSession session) {
        return ROLE_TESTER.equals(getCurrentRole(session));
    }
    
    /**
     * Check if user can add/edit (Admin or Business Manager)
     */
    public static boolean canAddEdit(HttpSession session) {
        String role = getCurrentRole(session);
        return ROLE_ADMIN.equals(role) || ROLE_BUSINESS_MANAGER.equals(role);
    }
    
    /**
     * Check if user can view only (Tester)
     */
    public static boolean isReadOnly(HttpSession session) {
        return ROLE_TESTER.equals(getCurrentRole(session));
    }
    
    /**
     * Check if user can manage users (Admin or Business Manager)
     */
    public static boolean canManageUsers(HttpSession session) {
        String role = getCurrentRole(session);
        return ROLE_ADMIN.equals(role) || ROLE_BUSINESS_MANAGER.equals(role);
    }
    
    /**
     * Check if user can manage projects/applications/modules (Admin or Business Manager)
     */
    public static boolean canManageProjects(HttpSession session) {
        String role = getCurrentRole(session);
        return ROLE_ADMIN.equals(role) || ROLE_BUSINESS_MANAGER.equals(role);
    }
}























