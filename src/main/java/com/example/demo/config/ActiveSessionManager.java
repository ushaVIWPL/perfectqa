package com.example.demo.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

@Component
public class ActiveSessionManager implements HttpSessionListener {

    // Maps normalized userId (lowercase) to the current active HttpSession
    private final Map<String, HttpSession> activeSessions = new ConcurrentHashMap<>();

    public void registerSession(String userId, HttpSession session) {
        if (userId == null || session == null) {
            return;
        }
        String normalizedUserId = userId.trim().toLowerCase();

        // Invalidate or flag any existing session for this user
        HttpSession oldSession = activeSessions.put(normalizedUserId, session);
        if (oldSession != null) {
            try {
                // We set an attribute to flag it so AuthFilter can handle it nicely and log them out
                oldSession.setAttribute("forceLogout", "concurrent_login");
            } catch (IllegalStateException e) {
                // Session might already be invalidated
            }
        }
        
        // Also bind the userId to the session so we can clean it up on destruction
        session.setAttribute("session_bound_userid", normalizedUserId);
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // No action needed when session is created (user is not yet authenticated)
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        try {
            String userId = (String) session.getAttribute("session_bound_userid");
            if (userId != null) {
                // Only remove from map if the session stored in the map matches this destroying session
                activeSessions.computeIfPresent(userId, (key, currentSession) -> {
                    if (currentSession.getId().equals(session.getId())) {
                        return null; // removes from map
                    }
                    return currentSession;
                });
            }
        } catch (IllegalStateException e) {
            // Session attributes cannot be accessed if session is already invalidated
        }
    }
}
