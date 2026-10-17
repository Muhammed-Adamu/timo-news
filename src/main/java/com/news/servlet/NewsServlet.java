package com.news.servlet;

import com.news.dao.CommentDAO;
import com.news.dao.NewsDAO;
import com.news.model.Comment;
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
 * Displays a single news article with its comments and related articles.
 */
@WebServlet("/news")
public class NewsServlet extends HttpServlet {

    private final NewsDAO newsDAO = new NewsDAO();
    private final CommentDAO commentDAO = new CommentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String slug = req.getParameter("slug");
        String idStr = req.getParameter("id");

        if (slug == null && idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        HttpSession session = req.getSession(false);
        Long currentUserId = null;
        if (session != null && session.getAttribute("user") != null) {
            currentUserId = ((User) session.getAttribute("user")).getId();
        }

        News news = null;
        if (slug != null) {
            news = newsDAO.findBySlug(slug, currentUserId);
        } else {
            try {
                Long id = Long.parseLong(idStr);
                news = newsDAO.findById(id);
                if (news != null && currentUserId != null) {
                    // Re-fetch to get like status
                    news = newsDAO.findBySlug(news.getSlug(), currentUserId);
                }
            } catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
        }

        if (news == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Article not found");
            return;
        }

        // Increment view count (async-like, but synchronous for simplicity)
        newsDAO.incrementViewCount(news.getId());

        // Fetch comments
        List<Comment> comments = commentDAO.findByNewsId(news.getId());

        // Fetch related articles
        List<News> relatedNews = newsDAO.findRelated(news.getId(), news.getCategoryId(), 4);

        req.setAttribute("news", news);
        req.setAttribute("comments", comments);
        req.setAttribute("relatedNews", relatedNews);
        req.setAttribute("pageTitle", news.getTitle() + " - TimoNews");

        req.getRequestDispatcher("/pages/news-detail.html").forward(req, resp);
    }
}
