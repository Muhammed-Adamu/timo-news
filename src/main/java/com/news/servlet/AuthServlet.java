package com.news.servlet;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.news.dao.UserDAO;
import com.news.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles user authentication: login, register, and logout.
 */
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("logout".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/home?message=logged_out");
            return;
        }

        if ("register".equals(action)) {
            req.setAttribute("pageTitle", "Create Account - TimoNews");
            req.getRequestDispatcher("/pages/register.html").forward(req, resp);
            return;
        }

        // Default: show login page
        req.setAttribute("pageTitle", "Login - TimoNews");
        req.getRequestDispatcher("/pages/login.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("login".equals(action)) {
            handleLogin(req, resp);
        } else if ("register".equals(action)) {
            handleRegister(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            req.setAttribute("error", "Please enter both username and password.");
            req.setAttribute("pageTitle", "Login - TimoNews");
            req.getRequestDispatcher("/pages/login.html").forward(req, resp);
            return;
        }

        User user = userDAO.findByUsername(username.trim());

        if (user == null || !BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified) {
            req.setAttribute("error", "Invalid username or password.");
            req.setAttribute("pageTitle", "Login - TimoNews");
            req.getRequestDispatcher("/pages/login.html").forward(req, resp);
            return;
        }

        // Login successful
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        // Redirect to originally requested page or home
        String redirect = (String) session.getAttribute("redirectAfterLogin");
        if (redirect != null) {
            session.removeAttribute("redirectAfterLogin");
            resp.sendRedirect(redirect);
        } else {
            resp.sendRedirect(req.getContextPath() + "/home?message=welcome");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirm_password");
        String email = req.getParameter("email");
        String fullName = req.getParameter("full_name");

        // Validation
        StringBuilder errors = new StringBuilder();
        if (username == null || username.trim().length() < 3) {
            errors.append("Username must be at least 3 characters. ");
        }
        if (password == null || password.length() < 6) {
            errors.append("Password must be at least 6 characters. ");
        }
        if (!password.equals(confirmPassword)) {
            errors.append("Passwords do not match. ");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errors.append("Please enter a valid email address. ");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.append("Please enter your full name. ");
        }

        if (errors.length() > 0) {
            req.setAttribute("error", errors.toString());
            req.setAttribute("username", username);
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.setAttribute("pageTitle", "Create Account - TimoNews");
            req.getRequestDispatcher("/pages/register.html").forward(req, resp);
            return;
        }

        // Check uniqueness
        if (userDAO.findByUsername(username.trim()) != null) {
            req.setAttribute("error", "Username is already taken.");
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.setAttribute("pageTitle", "Create Account - TimoNews");
            req.getRequestDispatcher("/pages/register.html").forward(req, resp);
            return;
        }
        if (userDAO.findByEmail(email.trim()) != null) {
            req.setAttribute("error", "Email is already registered.");
            req.setAttribute("username", username);
            req.setAttribute("fullName", fullName);
            req.setAttribute("pageTitle", "Create Account - TimoNews");
            req.getRequestDispatcher("/pages/register.html").forward(req, resp);
            return;
        }

        // Create user
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(hashedPassword);
        user.setEmail(email.trim());
        user.setFullName(fullName.trim());
        user.setRole("USER");

        if (userDAO.create(user)) {
            // Auto-login after registration
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            resp.sendRedirect(req.getContextPath() + "/home?message=registered");
        } else {
            req.setAttribute("error", "Registration failed. Please try again.");
            req.setAttribute("pageTitle", "Create Account - TimoNews");
            req.getRequestDispatcher("/pages/register.html").forward(req, resp);
        }
    }
}
