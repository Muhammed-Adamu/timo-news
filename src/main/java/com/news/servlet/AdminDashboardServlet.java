package com.news.servlet;

import com.news.dao.CategoryDAO;
import com.news.dao.NewsDAO;
import com.news.dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin dashboard — shows stats and quick overview.
 * Protected by AuthenticationFilter.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final NewsDAO newsDAO = new NewsDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long totalNews = newsDAO.countAll();
        long totalUsers = userDAO.countAll();
        long totalViews = newsDAO.totalViews();
        long totalCategories = categoryDAO.findAll().size();

        req.setAttribute("totalNews", totalNews);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalViews", totalViews);
        req.setAttribute("totalCategories", totalCategories);
        req.setAttribute("recentNews", newsDAO.findLatest(5));
        req.setAttribute("pageTitle", "Admin Dashboard - TimoNews");
        req.getRequestDispatcher("/admin/dashboard.html").forward(req, resp);
    }
}
