package com.news.listener;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Advanced Servlet Feature: ServletRequestListener
 * Logs every incoming HTTP request with timestamp, client IP, method, and URI.
 * Registered in web.xml (not via annotation to avoid duplicate registration).
 */
public class RequestLoggingListener implements ServletRequestListener {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        if (sre.getServletRequest() instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
            String timestamp = SDF.format(new Date());
            String ip = getClientIp(req);
            String method = req.getMethod();
            String uri = req.getRequestURI();
            String query = req.getQueryString();
            String fullUri = uri + (query != null ? "?" + query : "");

            System.out.println("[REQ] " + timestamp + " | " + ip + " | " + method + " " + fullUri);
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // No action needed on request completion
    }

    /**
     * Extracts the real client IP, accounting for proxies.
     */
    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        // If multiple IPs in X-Forwarded-For, take the first
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
