package com.news.servlet;

import com.news.dao.CategoryDAO;
import com.news.dao.NewsDAO;
import com.news.model.Category;
import com.news.model.News;
import com.news.model.PageInfo;
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
 * Handles full-text search across news articles with category and date filters.
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String query = req.getParameter("q");
        String categorySlug = req.getParameter("category");
        int page = 1;
        try {
            page = Integer.parseInt(req.getParameter("page"));
            if (page < 1) page = 1;
        } catch (NumberFormatException ignored) {}

        HttpSession session = req.getSession(false);
        Long currentUserId = null;
        if (session != null && session.getAttribute("user") != null) {
            currentUserId = ((User) session.getAttribute("user")).getId();
        }

        List<Category> categories = categoryDAO.findAll();

        if (query == null || query.trim().isEmpty()) {
            // No query — show search page with popular searches
            List<News> trending = newsDAO.findTrending(null, 8);
            req.setAttribute("trending", trending);
            req.setAttribute("categories", categories);
            req.setAttribute("pageTitle", "Search - TimoNews");
            req.getRequestDispatcher("/pages/search.html").forward(req, resp);
            return;
        }

        int pageSize = 10;
        PageInfo<News> pageInfo = newsDAO.search(query, categorySlug, page, pageSize, currentUserId);

        req.setAttribute("query", query);
        req.setAttribute("categorySlug", categorySlug);
        req.setAttribute("pageInfo", pageInfo);
        req.setAttribute("categories", categories);
        req.setAttribute("pageTitle", "Search: " + query + " - TimoNews");
        req.getRequestDispatcher("/pages/search.html").forward(req, resp);
    }
}
