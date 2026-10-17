package com.news.filter;

import com.news.model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Advanced Servlet Feature #2: AuthenticationFilter
 * Protects /admin/* routes by checking for an authenticated admin user.
 * Mapped in web.xml (not via annotation to avoid duplicate mapping).
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        boolean loggedIn = (session != null && session.getAttribute("user") != null);
        boolean isAdmin = false;

        if (loggedIn) {
            User user = (User) session.getAttribute("user");
            isAdmin = "ADMIN".equals(user.getRole());
        }

        if (!loggedIn || !isAdmin) {
            // For API requests, return JSON 401
            if (req.getRequestURI().startsWith(req.getContextPath() + "/api/")) {
                resp.setContentType("application/json; charset=UTF-8");
                resp.setStatus(401);
                resp.getWriter().write("{\"success\":false,\"error\":{\"message\":\"Authentication required\",\"code\":401}}");
                return;
            }
            // For page requests, redirect to login
            if (loggedIn && !isAdmin) {
                resp.sendRedirect(req.getContextPath() + "/home?error=access_denied");
            } else {
                session = req.getSession(true);
                session.setAttribute("redirectAfterLogin", req.getRequestURI());
                resp.sendRedirect(req.getContextPath() + "/pages/login.html");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
