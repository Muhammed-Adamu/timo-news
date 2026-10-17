package com.news.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Advanced Servlet Feature: HttpSessionListener
 * Tracks the number of active user sessions and exposes it via ServletContext.
 * Registered in web.xml (not via annotation to avoid duplicate registration).
 */
public class SessionListener implements HttpSessionListener {

    private static final String ACTIVE_SESSIONS_KEY = "activeSessionCount";

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext();
        Integer count = (Integer) ctx.getAttribute(ACTIVE_SESSIONS_KEY);
        if (count == null) count = 0;
        ctx.setAttribute(ACTIVE_SESSIONS_KEY, count + 1);
        System.out.println("[SessionListener] Session created. Active sessions: " + (count + 1)
                + " | Session ID: " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext();
        Integer count = (Integer) ctx.getAttribute(ACTIVE_SESSIONS_KEY);
        if (count == null) count = 1;
        int newCount = Math.max(0, count - 1);
        ctx.setAttribute(ACTIVE_SESSIONS_KEY, newCount);
        System.out.println("[SessionListener] Session destroyed. Active sessions: " + newCount);
    }
}
