package com.news.servlet;

import com.news.dao.CategoryDAO;
import com.news.dao.NewsDAO;
import com.news.model.Category;
import com.news.model.News;
import com.news.model.PageInfo;
import com.news.model.User;
import com.news.util.StringUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Admin news management — create, edit, delete news articles.
 * Protected by AuthenticationFilter.
 */
@WebServlet({"/admin/news-editor", "/admin/news-editor/*"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,     // 1 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 50     // 50 MB
)
public class AdminNewsServlet extends HttpServlet {

    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.startsWith("/edit")) {
            // Edit mode: load existing news
            String idStr = req.getParameter("id");
            if (idStr != null) {
                try {
                    Long id = Long.parseLong(idStr);
                    News news = newsDAO.findById(id);
                    if (news != null) {
                        req.setAttribute("editNews", news);
                    }
                } catch (NumberFormatException ignored) {}
            }
        } else if (pathInfo != null && pathInfo.startsWith("/delete")) {
            // Delete
            String idStr = req.getParameter("id");
            if (idStr != null) {
                try {
                    Long id = Long.parseLong(idStr);
                    newsDAO.delete(id);
                    resp.sendRedirect(req.getContextPath() + "/admin/news-editor?deleted=1");
                    return;
                } catch (NumberFormatException ignored) {}
            }
        }

        // List mode: show all news with pagination
        int page = 1;
        try {
            page = Integer.parseInt(req.getParameter("page"));
            if (page < 1) page = 1;
        } catch (NumberFormatException ignored) {}

        PageInfo<News> pageInfo = newsDAO.findAll(page, 10);
        List<Category> categories = categoryDAO.findAll();

        req.setAttribute("pageInfo", pageInfo);
        req.setAttribute("categories", categories);
        req.setAttribute("pageTitle", "Manage News - TimoNews Admin");
        req.getRequestDispatcher("/admin/news-editor.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        String action = req.getParameter("action");
        String idStr = req.getParameter("id");

        News news;
        if (idStr != null && !idStr.isEmpty() && !"create".equals(action)) {
            news = newsDAO.findById(Long.parseLong(idStr));
            if (news == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/news-editor?error=not_found");
                return;
            }
        } else {
            news = new News();
            news.setAuthorId(currentUser.getId());
        }

        // Get form fields
        String title = req.getParameter("title");
        String content = req.getParameter("content");
        String summary = req.getParameter("summary");
        String categoryIdStr = req.getParameter("category_id");
        String status = req.getParameter("status");
        String featured = req.getParameter("is_featured");
        String breaking = req.getParameter("is_breaking");

        // Validate required fields
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            req.setAttribute("error", "Title and content are required.");
            doGet(req, resp);
            return;
        }

        news.setTitle(title.trim());
        news.setSlug(StringUtil.toSlug(title.trim()));
        news.setContent(content);
        news.setSummary(summary != null ? summary.trim() :
                StringUtil.truncate(StringUtil.stripHtml(content), 200));
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            news.setCategoryId(Long.parseLong(categoryIdStr));
        }
        news.setStatus(status != null ? status : "PUBLISHED");
        news.setFeatured("on".equals(featured) || "1".equals(featured));
        news.setBreaking("on".equals(breaking) || "1".equals(breaking));

        // Handle image upload
        Part filePart = req.getPart("image");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = UUID.randomUUID().toString() + "_" + getFileName(filePart);
            String uploadPath = getServletContext().getRealPath("/assets/images/");

            // Ensure directory exists
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            filePart.write(uploadPath + File.separator + fileName);
            news.setImageUrl("assets/images/" + fileName);
        } else if (news.getImageUrl() == null) {
            news.setImageUrl("default-news.jpg");
        }

        if (newsDAO.save(news)) {
            resp.sendRedirect(req.getContextPath() + "/admin/news-editor?saved=1");
        } else {
            req.setAttribute("error", "Failed to save article.");
            doGet(req, resp);
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                String name = token.substring(token.indexOf('=') + 2, token.length() - 1);
                return name.substring(name.lastIndexOf('\\') + 1);
            }
        }
        return "upload.jpg";
    }
}
