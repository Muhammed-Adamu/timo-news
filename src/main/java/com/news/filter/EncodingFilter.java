package com.news.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Advanced Servlet Feature #1: EncodingFilter
 * Sets UTF-8 encoding on all incoming requests and outgoing responses.
 * Mapped in web.xml (not via annotation to avoid duplicate mapping).
 */
public class EncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Set UTF-8 encoding for request parameters only.
        // Do NOT set response content type globally — let each servlet
        // (or Tomcat's DefaultServlet for static files) set the correct MIME type.
        req.setCharacterEncoding("UTF-8");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
