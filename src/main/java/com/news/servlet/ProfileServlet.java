package com.news.servlet;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.news.dao.LikeDAO;
import com.news.dao.NewsDAO;
import com.news.dao.UserDAO;
import com.news.model.News;
import com.news.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User profile page — view/edit profile info, liked articles.
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final NewsDAO newsDAO = new NewsDAO();
    private final LikeDAO likeDAO = new LikeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        User freshUser = userDAO.findById(currentUser.getId());
        if (freshUser != null) {
            session.setAttribute("user", freshUser);
            currentUser = freshUser;
        }

        // Get liked articles
        List<Long> likedIds = likeDAO.findLikedNewsIdsByUser(currentUser.getId());
        List<News> likedNews = new ArrayList<>();
        for (Long newsId : likedIds) {
            News news = newsDAO.findById(newsId);
            if (news != null) likedNews.add(news);
        }

        req.setAttribute("profileUser", currentUser);
        req.setAttribute("likedNews", likedNews);
        req.setAttribute("pageTitle", "My Profile - TimoNews");
        req.getRequestDispatcher("/pages/profile.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        String action = req.getParameter("action");

        if ("update_profile".equals(action)) {
            String fullName = req.getParameter("full_name");
            String email = req.getParameter("email");
            String bio = req.getParameter("bio");

            if (fullName == null || fullName.trim().isEmpty()) {
                req.setAttribute("error", "Name cannot be empty.");
                doGet(req, resp);
                return;
            }

            currentUser.setFullName(fullName.trim());
            currentUser.setEmail(email != null ? email.trim() : currentUser.getEmail());
            currentUser.setBio(bio != null ? bio.trim() : "");

            if (userDAO.updateProfile(currentUser)) {
                session.setAttribute("user", currentUser);
                req.setAttribute("success", "Profile updated successfully.");
            } else {
                req.setAttribute("error", "Failed to update profile.");
            }

        } else if ("change_password".equals(action)) {
            String currentPassword = req.getParameter("current_password");
            String newPassword = req.getParameter("new_password");
            String confirmPassword = req.getParameter("confirm_password");

            User dbUser = userDAO.findById(currentUser.getId());
            if (!BCrypt.verifyer().verify(currentPassword.toCharArray(), dbUser.getPassword()).verified) {
                req.setAttribute("error", "Current password is incorrect.");
            } else if (newPassword == null || newPassword.length() < 6) {
                req.setAttribute("error", "New password must be at least 6 characters.");
            } else if (!newPassword.equals(confirmPassword)) {
                req.setAttribute("error", "New passwords do not match.");
            } else {
                String hashed = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
                if (userDAO.updatePassword(currentUser.getId(), hashed)) {
                    req.setAttribute("success", "Password changed successfully.");
                } else {
                    req.setAttribute("error", "Failed to change password.");
                }
            }
        }

        doGet(req, resp);
    }
}
