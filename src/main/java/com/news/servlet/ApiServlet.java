package com.news.servlet;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.news.dao.*;
import com.news.model.*;
import com.news.util.DBUtil;
import com.news.util.StringUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST API Servlet — handles ALL /api/* routes.
 * Pure JSON responses for front-end/back-end separation.
 * Mapped in web.xml (not via annotation to avoid duplicate mapping).
 */
public class ApiServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .serializeNulls()
            .disableHtmlEscaping()
            .create();
    private final NewsDAO newsDAO = new NewsDAO();
    private final CommentDAO commentDAO = new CommentDAO();
    private final LikeDAO likeDAO = new LikeDAO();
    private final ContactDAO contactDAO = new ContactDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ==================== GET ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String p = path(req);
        try {
            String json;
            if      (p.equals("/auth/me"))                  json = handleAuthMe(req);
            else if (p.equals("/auth/liked-articles"))       json = handleLikedArticles(req);
            else if (p.equals("/news/featured"))             json = handleFeatured(req);
            else if (p.equals("/news/trending"))             json = handleTrending(req);
            else if (p.equals("/news/breaking"))             json = handleBreaking(req);
            else if (p.equals("/news"))                      json = handleNewsList(req);
            else if (p.matches("/news/detail/[^/]+"))        json = handleGetNewsBySlug(p, req);
            else if (p.matches("/news/\\d+/comments"))       json = handleGetComments(p, req);
            else if (p.matches("/news/\\d+/related"))        json = handleRelated(p, req);
            else if (p.matches("/news/\\d+"))                json = handleGetNews(p, req);
            else if (p.equals("/search"))                    json = handleSearch(req);
            else if (p.equals("/categories"))                json = handleCategories();
            else if (p.equals("/health"))                    json = handleHealth();
            else if (p.equals("/admin/stats"))               json = handleAdminStats(req);
            else if (p.equals("/admin/news"))                json = handleAdminNewsList(req);
            else if (p.matches("/admin/news/\\d+"))          json = handleAdminGetNews(p, req);
            else { json = errorJson("Endpoint not found", 404); resp.setStatus(404); }
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(errorJson("Internal server error: " + e.getMessage(), 500));
        }
    }

    // ==================== POST ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String p = path(req);
        try {
            String json;
            if      (p.equals("/auth/login"))                json = handleLogin(req);
            else if (p.equals("/auth/register"))             json = handleRegister(req);
            else if (p.equals("/auth/logout"))               json = handleLogout(req);
            else if (p.equals("/auth/update-profile"))       json = handleUpdateProfile(req);
            else if (p.equals("/auth/change-password"))      json = handleChangePassword(req);
            else if (p.matches("/news/\\d+/comments"))       json = handlePostComment(p, req, resp);
            else if (p.matches("/news/\\d+/like"))           json = handleToggleLike(p, req, resp);
            else if (p.equals("/contact"))                   json = handleContactForm(req);
            else if (p.equals("/search/autocomplete"))       json = handleAutocomplete(req);
            else if (p.equals("/admin/news"))                json = handleAdminCreateNews(req);
            else if (p.matches("/admin/news/\\d+"))          json = handleAdminUpdateNews(p, req);
            else if (p.equals("/admin/categories"))          json = handleAdminCreateCategory(req);
            else if (p.matches("/admin/categories/\\d+"))    json = handleAdminUpdateCategory(p, req);
            else { json = errorJson("Endpoint not found", 404); resp.setStatus(404); }
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(errorJson("Internal server error: " + e.getMessage(), 500));
        }
    }

    // ==================== PUT ====================
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String p = path(req);
        try {
            String json;
            if      (p.matches("/admin/news/\\d+"))          json = handleAdminUpdateNews(p, req);
            else if (p.matches("/admin/categories/\\d+"))    json = handleAdminUpdateCategory(p, req);
            else { json = errorJson("Endpoint not found", 404); resp.setStatus(404); }
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(errorJson("Internal server error: " + e.getMessage(), 500));
        }
    }

    // ==================== DELETE ====================
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String p = path(req);
        try {
            String json;
            if      (p.matches("/admin/news/\\d+"))          json = handleAdminDeleteNews(p);
            else if (p.matches("/admin/categories/\\d+"))    json = handleAdminDeleteCategory(p);
            else { json = errorJson("Endpoint not found", 404); resp.setStatus(404); }
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(errorJson("Internal server error: " + e.getMessage(), 500));
        }
    }

    // ==================== Auth Handlers ====================
    private String handleAuthMe(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("user") == null) return errorJson("Not authenticated", 401);
        User u = (User) s.getAttribute("user");
        User fresh = userDAO.findById(u.getId());
        if (fresh != null) {
            s.setAttribute("user", fresh);
            return successJson(toSafeUser(fresh));
        }
        return successJson(toSafeUser(u));
    }

    private String handleLogin(HttpServletRequest req) {
        Map<String, String> body = parseJsonBody(req);
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) return errorJson("Username and password required", 400);
        User u = userDAO.findByUsername(username.trim());
        if (u == null || !BCrypt.verifyer().verify(password.toCharArray(), u.getPassword()).verified)
            return errorJson("Invalid username or password", 401);
        req.getSession(true).setAttribute("user", u);
        return successJson(toSafeUser(u));
    }

    private String handleRegister(HttpServletRequest req) {
        Map<String, String> body = parseJsonBody(req);
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");
        String fullName = body.get("full_name");
        if (username == null || username.trim().length() < 3) return errorJson("Username must be at least 3 characters", 400);
        if (password == null || password.length() < 6) return errorJson("Password must be at least 6 characters", 400);
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) return errorJson("Valid email required", 400);
        if (fullName == null || fullName.trim().isEmpty()) return errorJson("Full name required", 400);
        if (userDAO.findByUsername(username.trim()) != null) return errorJson("Username already taken", 409);
        if (userDAO.findByEmail(email.trim()) != null) return errorJson("Email already registered", 409);

        User u = new User();
        u.setUsername(username.trim());
        u.setPassword(BCrypt.withDefaults().hashToString(12, password.toCharArray()));
        u.setEmail(email.trim());
        u.setFullName(fullName.trim());
        u.setRole("USER");
        if (userDAO.create(u)) {
            req.getSession(true).setAttribute("user", u);
            return successJson(toSafeUser(u));
        }
        return errorJson("Registration failed", 500);
    }

    private String handleLogout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        return successJson("Logged out");
    }

    private String handleUpdateProfile(HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null) return errorJson("Authentication required", 401);
        Map<String, String> body = parseJsonBody(req);
        String fullName = body.get("full_name");
        String email = body.get("email");
        String bio = body.get("bio");
        if (fullName == null || fullName.trim().isEmpty()) return errorJson("Name required", 400);
        u.setFullName(fullName.trim());
        if (email != null) u.setEmail(email.trim());
        u.setBio(bio != null ? bio.trim() : "");
        if (userDAO.updateProfile(u)) {
            req.getSession().setAttribute("user", u);
            return successJson(toSafeUser(u));
        }
        return errorJson("Update failed", 500);
    }

    private String handleChangePassword(HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null) return errorJson("Authentication required", 401);
        Map<String, String> body = parseJsonBody(req);
        String current = body.get("current_password");
        String newPw = body.get("new_password");
        if (current == null || newPw == null) return errorJson("Both passwords required", 400);
        if (newPw.length() < 6) return errorJson("New password must be at least 6 characters", 400);
        User db = userDAO.findById(u.getId());
        if (!BCrypt.verifyer().verify(current.toCharArray(), db.getPassword()).verified)
            return errorJson("Current password is incorrect", 400);
        String hashed = BCrypt.withDefaults().hashToString(12, newPw.toCharArray());
        if (userDAO.updatePassword(u.getId(), hashed)) return successJson("Password changed");
        return errorJson("Failed to change password", 500);
    }

    private String handleLikedArticles(HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null) return errorJson("Authentication required", 401);
        List<Long> ids = likeDAO.findLikedNewsIdsByUser(u.getId());
        List<News> liked = new ArrayList<>();
        for (Long id : ids) {
            News n = newsDAO.findById(id);
            if (n != null) liked.add(n);
        }
        return successJson(liked);
    }

    // ==================== News Handlers ====================
    private String handleFeatured(HttpServletRequest req) {
        return successJson(newsDAO.findFeatured(getUserId(req)));
    }
    private String handleTrending(HttpServletRequest req) {
        int limit = parseInt(req, "limit", 5);
        return successJson(newsDAO.findTrending(getUserId(req), limit));
    }
    private String handleBreaking(HttpServletRequest req) {
        return successJson(newsDAO.findBreaking(getUserId(req)));
    }

    private String handleNewsList(HttpServletRequest req) {
        int page = parseInt(req, "page", 1);
        int limit = parseInt(req, "limit", 10);
        String category = req.getParameter("category");
        String slug = req.getParameter("slug");
        if (slug != null && !slug.isEmpty()) {
            News n = newsDAO.findBySlug(slug, getUserId(req));
            return n != null ? successJson(n) : errorJson("Not found", 404);
        }
        return successJson(newsDAO.findPublished(page, limit, category, getUserId(req)));
    }

    private String handleGetNews(String path, HttpServletRequest req) {
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        News n = newsDAO.findById(id);
        if (n == null) return errorJson("Not found", 404);
        newsDAO.incrementViewCount(id);
        Long uid = getUserId(req);
        if (uid != null) n = newsDAO.findBySlug(n.getSlug(), uid);
        return successJson(n);
    }

    private String handleGetNewsBySlug(String path, HttpServletRequest req) {
        String slug = path.substring(path.lastIndexOf('/') + 1);
        News n = newsDAO.findBySlug(slug, getUserId(req));
        if (n == null) return errorJson("Not found", 404);
        newsDAO.incrementViewCount(n.getId());
        return successJson(n);
    }

    private String handleGetComments(String path, HttpServletRequest req) {
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        return successJson(commentDAO.findByNewsId(id));
    }

    private String handleRelated(String path, HttpServletRequest req) {
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        News n = newsDAO.findById(id);
        if (n == null) return errorJson("Not found", 404);
        return successJson(newsDAO.findRelated(id, n.getCategoryId(), 4));
    }

    // ==================== Comment / Like ====================
    private String handlePostComment(String path, HttpServletRequest req, HttpServletResponse resp) {
        Long newsId = extractId(path);
        if (newsId == null) return errorJson("Invalid ID", 400);
        User u = requireAuth(req);
        if (u == null) { resp.setStatus(401); return errorJson("Login required", 401); }
        Map<String, String> body = parseJsonBody(req);
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) return errorJson("Content required", 400);
        Comment c = new Comment();
        c.setNewsId(newsId); c.setUserId(u.getId()); c.setContent(content.trim());
        if (commentDAO.create(c)) {
            Comment saved = commentDAO.findById(c.getId());
            return successJson(saved);
        }
        return errorJson("Failed to save", 500);
    }

    private String handleToggleLike(String path, HttpServletRequest req, HttpServletResponse resp) {
        Long newsId = extractId(path);
        if (newsId == null) return errorJson("Invalid ID", 400);
        User u = requireAuth(req);
        if (u == null) { resp.setStatus(401); return errorJson("Login required", 401); }
        boolean liked = likeDAO.toggle(newsId, u.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeDAO.countByNewsId(newsId));
        return GSON.toJson(result);
    }

    // ==================== Search ====================
    private String handleSearch(HttpServletRequest req) {
        String q = req.getParameter("q");
        String cat = req.getParameter("category");
        int page = parseInt(req, "page", 1);
        int limit = parseInt(req, "limit", 10);
        Long uid = getUserId(req);
        if (q == null || q.trim().isEmpty()) return successJson(Collections.emptyList());
        return successJson(newsDAO.search(q, cat, page, limit, uid));
    }

    private String handleAutocomplete(HttpServletRequest req) {
        Map<String, String> body = parseJsonBody(req);
        String q = body.get("q");
        if (q == null || q.trim().length() < 2) return successJson(Collections.emptyList());
        PageInfo<News> results = newsDAO.search(q.trim(), null, 1, 6, null);
        List<Map<String, String>> suggestions = new ArrayList<>();
        for (News n : results.getItems()) {
            Map<String, String> item = new HashMap<>();
            item.put("title", n.getTitle()); item.put("slug", n.getSlug()); item.put("category", n.getCategoryName());
            suggestions.add(item);
        }
        return successJson(suggestions);
    }

    // ==================== Categories ====================
    private String handleCategories() { return successJson(categoryDAO.findAll()); }

    // ==================== Contact ====================
    private String handleContactForm(HttpServletRequest req) {
        Map<String, String> body = parseJsonBody(req);
        String name = body.get("name"), email = body.get("email"), subject = body.get("subject"), message = body.get("message");
        if (name == null || name.trim().isEmpty()) return errorJson("Name required", 400);
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) return errorJson("Valid email required", 400);
        if (message == null || message.trim().isEmpty()) return errorJson("Message required", 400);
        Contact c = new Contact();
        c.setName(name.trim()); c.setEmail(email.trim());
        c.setSubject(subject != null ? subject.trim() : "General Inquiry"); c.setMessage(message.trim());
        if (contactDAO.save(c)) return successJson("Message sent successfully!");
        return errorJson("Failed to send", 500);
    }

    // ==================== Health Check ====================
    private String handleHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("app", "TimoNews");
        health.put("status", "running");
        health.put("java", System.getProperty("java.version"));
        try {
            Connection conn = DBUtil.getConnection();
            health.put("database", "connected");
            // Check if tables have data
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM news");
                rs.next();
                health.put("newsCount", rs.getLong(1));
                rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
                rs.next();
                health.put("categoryCount", rs.getLong(1));
                rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                rs.next();
                health.put("userCount", rs.getLong(1));
            }
            conn.close();
        } catch (SQLException e) {
            health.put("database", "ERROR: " + e.getMessage());
        }
        return successJson(health);
    }

    // ==================== Admin: Stats ====================
    private String handleAdminStats(HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null || !u.isAdmin()) return errorJson("Admin access required", 403);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNews", newsDAO.countAll());
        stats.put("totalUsers", userDAO.countAll());
        stats.put("totalViews", newsDAO.totalViews());
        stats.put("totalCategories", categoryDAO.findAll().size());
        stats.put("recentNews", newsDAO.findLatest(5));
        return successJson(stats);
    }

    // ==================== Admin: News ====================
    private String handleAdminNewsList(HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null || !u.isAdmin()) return errorJson("Admin access required", 403);
        int page = parseInt(req, "page", 1);
        int limit = parseInt(req, "limit", 50);
        return successJson(newsDAO.findAll(page, limit));
    }

    private String handleAdminGetNews(String path, HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null || !u.isAdmin()) return errorJson("Admin access required", 403);
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        News n = newsDAO.findById(id);
        return n != null ? successJson(n) : errorJson("Not found", 404);
    }

    private String handleAdminCreateNews(HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null || !u.isAdmin()) return errorJson("Admin access required", 403);
        try {
            News news = new News();
            news.setAuthorId(u.getId());
            populateNewsFromRequest(news, req);
            if (newsDAO.save(news)) return successJson(news);
            return errorJson("Failed to save", 500);
        } catch (Exception e) {
            return errorJson("Error: " + e.getMessage(), 500);
        }
    }

    private String handleAdminUpdateNews(String path, HttpServletRequest req) {
        User u = requireAuth(req);
        if (u == null || !u.isAdmin()) return errorJson("Admin access required", 403);
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        News news = newsDAO.findById(id);
        if (news == null) return errorJson("Not found", 404);
        try {
            populateNewsFromRequest(news, req);
            if (newsDAO.save(news)) return successJson(news);
            return errorJson("Failed to update", 500);
        } catch (Exception e) {
            return errorJson("Error: " + e.getMessage(), 500);
        }
    }

    private String handleAdminDeleteNews(String path) {
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        if (newsDAO.delete(id)) return successJson("Deleted");
        return errorJson("Delete failed", 500);
    }

    private void populateNewsFromRequest(News news, HttpServletRequest req) throws Exception {
        String title = null, content = null, summary = null, catId = null, status = null;
        String featured = null, breaking = null;

        // Check if multipart
        String contentType = req.getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            for (Part part : req.getParts()) {
                String name = part.getName();
                if ("title".equals(name)) title = readPart(part);
                else if ("content".equals(name)) content = readPart(part);
                else if ("summary".equals(name)) summary = readPart(part);
                else if ("category_id".equals(name)) catId = readPart(part);
                else if ("status".equals(name)) status = readPart(part);
                else if ("is_featured".equals(name)) featured = readPart(part);
                else if ("is_breaking".equals(name)) breaking = readPart(part);
                else if ("image".equals(name) && part.getSize() > 0) {
                    String fn = UUID.randomUUID() + "_" + getFileName(part);
                    String uploadPath = getServletContext().getRealPath("/assets/images/");
                    new File(uploadPath).mkdirs();
                    part.write(uploadPath + File.separator + fn);
                    news.setImageUrl("assets/images/" + fn);
                }
            }
        } else {
            Map<String, String> body = parseJsonBody(req);
            title = body.get("title"); content = body.get("content"); summary = body.get("summary");
            catId = body.get("category_id"); status = body.get("status");
            featured = body.get("is_featured"); breaking = body.get("is_breaking");
        }

        if (title != null && !title.trim().isEmpty()) {
            news.setTitle(title.trim());
            news.setSlug(StringUtil.toSlug(title.trim()));
        }
        if (content != null) news.setContent(content);
        if (summary != null) news.setSummary(summary.trim());
        if (catId != null && !catId.isEmpty()) news.setCategoryId(Long.parseLong(catId));
        if (status != null) news.setStatus(status);
        news.setFeatured("on".equals(featured) || "1".equals(featured) || "true".equalsIgnoreCase(featured));
        news.setBreaking("on".equals(breaking) || "1".equals(breaking) || "true".equalsIgnoreCase(breaking));
        if (news.getImageUrl() == null) news.setImageUrl("default-news.jpg");
    }

    // ==================== Admin: Categories ====================
    private String handleAdminCreateCategory(HttpServletRequest req) {
        Map<String, String> body = parseJsonBody(req);
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) return errorJson("Name required", 400);
        Category cat = new Category();
        cat.setName(name.trim());
        cat.setSlug(StringUtil.toSlug(name.trim()));
        cat.setDescription(body.getOrDefault("description", ""));
        cat.setIconClass(body.getOrDefault("icon_class", "bi-folder"));
        cat.setColor(body.getOrDefault("color", "#6c757d"));
        try { cat.setSortOrder(Integer.parseInt(body.getOrDefault("sort_order", "0"))); } catch (NumberFormatException e) { cat.setSortOrder(0); }
        if (categoryDAO.save(cat)) return successJson(cat);
        return errorJson("Failed to save", 500);
    }

    private String handleAdminUpdateCategory(String path, HttpServletRequest req) {
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        Category cat = categoryDAO.findById(id);
        if (cat == null) return errorJson("Not found", 404);
        Map<String, String> body = parseJsonBody(req);
        if (body.containsKey("name") && !body.get("name").trim().isEmpty()) {
            cat.setName(body.get("name").trim());
            cat.setSlug(StringUtil.toSlug(body.get("name").trim()));
        }
        if (body.containsKey("description")) cat.setDescription(body.get("description"));
        if (body.containsKey("icon_class")) cat.setIconClass(body.get("icon_class"));
        if (body.containsKey("color")) cat.setColor(body.get("color"));
        if (body.containsKey("sort_order")) {
            try { cat.setSortOrder(Integer.parseInt(body.get("sort_order"))); } catch (NumberFormatException ignored) {}
        }
        if (categoryDAO.save(cat)) return successJson(cat);
        return errorJson("Failed to update", 500);
    }

    private String handleAdminDeleteCategory(String path) {
        Long id = extractId(path);
        if (id == null) return errorJson("Invalid ID", 400);
        if (categoryDAO.delete(id)) return successJson("Deleted");
        return errorJson("Delete failed", 500);
    }

    // ==================== Helpers ====================
    private String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return p == null ? "/" : p;
    }

    private Long extractId(String path) {
        Matcher m = Pattern.compile("/(\\d+)").matcher(path);
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }

    private Long getUserId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s != null && s.getAttribute("user") != null) ? ((User) s.getAttribute("user")).getId() : null;
    }

    private User requireAuth(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s != null && s.getAttribute("user") != null) ? (User) s.getAttribute("user") : null;
    }

    private int parseInt(HttpServletRequest req, String name, int def) {
        try { String v = req.getParameter(name); return v != null ? Integer.parseInt(v) : def; } catch (NumberFormatException e) { return def; }
    }

    private Map<String, String> parseJsonBody(HttpServletRequest req) {
        try {
            BufferedReader r = req.getReader();
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = r.readLine()) != null) sb.append(line);
            @SuppressWarnings("unchecked")
            Map<String, String> map = GSON.fromJson(sb.toString(), Map.class);
            return map != null ? map : new HashMap<>();
        } catch (IOException e) { return new HashMap<>(); }
    }

    private String readPart(Part part) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder(); String line;
        while ((line = r.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private String getFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        for (String token : cd.split(";")) {
            if (token.trim().startsWith("filename")) {
                String name = token.substring(token.indexOf('=') + 2, token.length() - 1);
                return name.substring(name.lastIndexOf('\\') + 1);
            }
        }
        return "upload.jpg";
    }

    private Map<String, Object> toSafeUser(User u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId()); map.put("username", u.getUsername());
        map.put("email", u.getEmail()); map.put("fullName", u.getFullName());
        map.put("avatar", u.getAvatar()); map.put("role", u.getRole());
        map.put("bio", u.getBio()); map.put("createdAt", u.getCreatedAt());
        return map;
    }

    private String successJson(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true); result.put("data", data);
        return GSON.toJson(result);
    }

    private String errorJson(String message, int code) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", Map.of("message", message, "code", code));
        return GSON.toJson(result);
    }
}
