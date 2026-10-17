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
 * Displays news filtered by category with pagination.
 */
@WebServlet("/category")
public class CategoryServlet extends HttpServlet {

    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String slug = req.getParameter("slug");
        int page = 1;
        try {
            page = Integer.parseInt(req.getParameter("page"));
            if (page < 1) page = 1;
        } catch (NumberFormatException ignored) {}

        if (slug == null || slug.isEmpty()) {
            // Show all categories overview
            List<Category> categories = categoryDAO.findAll();
            req.setAttribute("categories", categories);
            req.setAttribute("pageTitle", "Categories - TimoNews");
            req.getRequestDispatcher("/pages/categories.html").forward(req, resp);
            return;
        }

        Category category = categoryDAO.findBySlug(slug);
        if (category == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Category not found");
            return;
        }

        HttpSession session = req.getSession(false);
        Long currentUserId = null;
        if (session != null && session.getAttribute("user") != null) {
            currentUserId = ((User) session.getAttribute("user")).getId();
        }

        int pageSize = 9;
        PageInfo<News> pageInfo = newsDAO.findPublished(page, pageSize, slug, currentUserId);

        req.setAttribute("category", category);
        req.setAttribute("pageInfo", pageInfo);
        req.setAttribute("pageTitle", category.getName() + " News - TimoNews");
        req.getRequestDispatcher("/pages/category.html").forward(req, resp);
    }
}
