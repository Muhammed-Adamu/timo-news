package com.news.servlet;

import com.news.dao.CategoryDAO;
import com.news.model.Category;
import com.news.util.StringUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Admin category management — CRUD for news categories.
 * Protected by AuthenticationFilter.
 */
@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Category> categories = categoryDAO.findAll();
        req.setAttribute("categories", categories);
        req.setAttribute("pageTitle", "Manage Categories - TimoNews Admin");
        req.getRequestDispatcher("/admin/categories.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("delete".equals(action)) {
            String idStr = req.getParameter("id");
            if (idStr != null) {
                try {
                    categoryDAO.delete(Long.parseLong(idStr));
                } catch (NumberFormatException ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/admin/categories?deleted=1");
            return;
        }

        // Create or update
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String iconClass = req.getParameter("icon_class");
        String color = req.getParameter("color");
        String sortOrderStr = req.getParameter("sort_order");

        if (name == null || name.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/categories?error=name_required");
            return;
        }

        Category category;
        if (idStr != null && !idStr.isEmpty()) {
            category = categoryDAO.findById(Long.parseLong(idStr));
            if (category == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/categories?error=not_found");
                return;
            }
        } else {
            category = new Category();
        }

        category.setName(name.trim());
        category.setSlug(StringUtil.toSlug(name.trim()));
        category.setDescription(description != null ? description.trim() : "");
        category.setIconClass(iconClass != null ? iconClass : "bi-folder");
        category.setColor(color != null ? color : "#6c757d");
        try {
            category.setSortOrder(Integer.parseInt(sortOrderStr != null ? sortOrderStr : "0"));
        } catch (NumberFormatException e) {
            category.setSortOrder(0);
        }

        if (categoryDAO.save(category)) {
            resp.sendRedirect(req.getContextPath() + "/admin/categories?saved=1");
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/categories?error=save_failed");
        }
    }
}
