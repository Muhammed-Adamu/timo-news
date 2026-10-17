package com.news.servlet;

import com.news.dao.CategoryDAO;
import com.news.dao.NewsDAO;
import com.news.model.Category;
import com.news.model.News;
import com.news.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Handles the homepage — featured news, breaking news, trending, and category display.
 */
@WebServlet({"/home", "/index"})
public class HomeServlet extends HttpServlet {

    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Long currentUserId = null;
        if (session != null && session.getAttribute("user") != null) {
            currentUserId = ((User) session.getAttribute("user")).getId();
        }

        // Fetch data for homepage
        List<News> featuredNews = newsDAO.findFeatured(currentUserId);
        List<News> breakingNews = newsDAO.findBreaking(currentUserId);
        List<News> trendingNews = newsDAO.findTrending(currentUserId, 5);
        List<News> latestNews = newsDAO.findLatest(6);
        List<Category> categories = categoryDAO.findAll();

        req.setAttribute("featuredNews", featuredNews);
        req.setAttribute("breakingNews", breakingNews);
        req.setAttribute("trendingNews", trendingNews);
        req.setAttribute("latestNews", latestNews);
        req.setAttribute("categories", categories);
        req.setAttribute("pageTitle", "TimoNews - Your Source for Breaking News");

        req.getRequestDispatcher("/index.html").forward(req, resp);
    }
}
