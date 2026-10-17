package com.news.dao;

import com.news.model.News;
import com.news.model.PageInfo;
import com.news.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NewsDAO {

    private static final String SELECT_JOIN =
            "SELECT n.*, c.name AS category_name, c.slug AS category_slug, c.color AS category_color, "
            + "u.full_name AS author_name, u.avatar AS author_avatar, "
            + "(SELECT COUNT(*) FROM comments cm WHERE cm.news_id = n.id AND cm.status = 'APPROVED') AS comment_count, "
            + "(SELECT COUNT(*) FROM likes lk WHERE lk.news_id = n.id) AS like_count "
            + "FROM news n "
            + "LEFT JOIN categories c ON n.category_id = c.id "
            + "LEFT JOIN users u ON n.author_id = u.id ";

    /**
     * Get paginated published news, optionally filtered by category slug.
     */
    public PageInfo<News> findPublished(int page, int pageSize, String categorySlug, Long currentUserId) {
        List<News> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_JOIN)
                .append("WHERE n.status = 'PUBLISHED' ");
        if (categorySlug != null && !categorySlug.isEmpty()) {
            sql.append("AND c.slug = ? ");
        }
        sql.append("ORDER BY n.created_at DESC LIMIT ? OFFSET ?");

        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM news n ")
                .append("LEFT JOIN categories c ON n.category_id = c.id ")
                .append("WHERE n.status = 'PUBLISHED' ");
        if (categorySlug != null && !categorySlug.isEmpty()) {
            countSql.append("AND c.slug = ?");
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Count
            long total;
            try (PreparedStatement ps = conn.prepareStatement(countSql.toString())) {
                int idx = 1;
                if (categorySlug != null && !categorySlug.isEmpty()) {
                    ps.setString(idx++, categorySlug);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }

            // Fetch
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                if (categorySlug != null && !categorySlug.isEmpty()) {
                    ps.setString(idx++, categorySlug);
                }
                ps.setInt(idx++, pageSize);
                ps.setInt(idx, (page - 1) * pageSize);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        News news = mapRow(rs);
                        if (currentUserId != null) {
                            news.setLikedByCurrentUser(isLikedBy(conn, news.getId(), currentUserId));
                        }
                        list.add(news);
                    }
                }
            }

            return new PageInfo<>(list, page, pageSize, total);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageInfo<>(list, page, pageSize, 0);
    }

    /**
     * Get featured news (for homepage hero/carousel).
     */
    public List<News> findFeatured(Long currentUserId) {
        List<News> list = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE n.status = 'PUBLISHED' AND n.is_featured = 1 "
                + "ORDER BY n.created_at DESC LIMIT 6";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                News news = mapRow(rs);
                if (currentUserId != null) {
                    news.setLikedByCurrentUser(isLikedBy(conn, news.getId(), currentUserId));
                }
                list.add(news);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get breaking news.
     */
    public List<News> findBreaking(Long currentUserId) {
        List<News> list = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE n.status = 'PUBLISHED' AND n.is_breaking = 1 "
                + "ORDER BY n.created_at DESC LIMIT 5";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                News news = mapRow(rs);
                if (currentUserId != null) {
                    news.setLikedByCurrentUser(isLikedBy(conn, news.getId(), currentUserId));
                }
                list.add(news);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get trending news (by view count).
     */
    public List<News> findTrending(Long currentUserId, int limit) {
        List<News> list = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE n.status = 'PUBLISHED' "
                + "ORDER BY n.view_count DESC LIMIT ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    News news = mapRow(rs);
                    if (currentUserId != null) {
                        news.setLikedByCurrentUser(isLikedBy(conn, news.getId(), currentUserId));
                    }
                    list.add(news);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get latest news (for sidebars).
     */
    public List<News> findLatest(int limit) {
        List<News> list = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE n.status = 'PUBLISHED' "
                + "ORDER BY n.created_at DESC LIMIT ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get single news by slug.
     */
    public News findBySlug(String slug, Long currentUserId) {
        String sql = SELECT_JOIN + "WHERE n.slug = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    News news = mapRow(rs);
                    if (currentUserId != null) {
                        news.setLikedByCurrentUser(isLikedBy(conn, news.getId(), currentUserId));
                    }
                    return news;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get single news by id.
     */
    public News findById(Long id) {
        String sql = SELECT_JOIN + "WHERE n.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Increment view count.
     */
    public void incrementViewCount(Long newsId) {
        String sql = "UPDATE news SET view_count = view_count + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search news by keyword.
     */
    public PageInfo<News> search(String keyword, String categorySlug, int page, int pageSize, Long currentUserId) {
        List<News> list = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE n.status = 'PUBLISHED' ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (n.title LIKE ? OR n.summary LIKE ? OR n.content LIKE ?) ");
        }
        if (categorySlug != null && !categorySlug.isEmpty()) {
            where.append("AND c.slug = ? ");
        }

        String countSql = "SELECT COUNT(*) FROM news n LEFT JOIN categories c ON n.category_id = c.id " + where;
        String dataSql = SELECT_JOIN + where + "ORDER BY n.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection()) {
            // Count
            long total = 0;
            try (PreparedStatement ps = conn.prepareStatement(countSql.toString())) {
                setSearchParams(ps, keyword, categorySlug);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getLong(1);
                }
            }

            // Fetch
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = setSearchParams(ps, keyword, categorySlug);
                ps.setInt(idx++, pageSize);
                ps.setInt(idx, (page - 1) * pageSize);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        News news = mapRow(rs);
                        if (currentUserId != null) {
                            news.setLikedByCurrentUser(isLikedBy(conn, news.getId(), currentUserId));
                        }
                        list.add(news);
                    }
                }
            }
            return new PageInfo<>(list, page, pageSize, total);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageInfo<>(list, page, pageSize, 0);
    }

    /**
     * Get related news (same category, excluding current).
     */
    public List<News> findRelated(Long newsId, Long categoryId, int limit) {
        List<News> list = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE n.status = 'PUBLISHED' AND n.id != ? AND n.category_id = ? "
                + "ORDER BY n.created_at DESC LIMIT ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
            ps.setLong(2, categoryId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ========== CRUD for Admin ==========

    public boolean save(News news) {
        if (news.getId() == null) {
            return insert(news);
        } else {
            return update(news);
        }
    }

    private boolean insert(News news) {
        String sql = "INSERT INTO news (title, slug, content, summary, image_url, category_id, "
                + "author_id, status, is_featured, is_breaking) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, news.getTitle());
            ps.setString(2, news.getSlug());
            ps.setString(3, news.getContent());
            ps.setString(4, news.getSummary());
            ps.setString(5, news.getImageUrl() != null ? news.getImageUrl() : "default-news.jpg");
            ps.setLong(6, news.getCategoryId());
            ps.setLong(7, news.getAuthorId());
            ps.setString(8, news.getStatus() != null ? news.getStatus() : "PUBLISHED");
            ps.setInt(9, news.isFeatured() ? 1 : 0);
            ps.setInt(10, news.isBreaking() ? 1 : 0);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) news.setId(keys.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean update(News news) {
        String sql = "UPDATE news SET title = ?, slug = ?, content = ?, summary = ?, image_url = ?, "
                + "category_id = ?, status = ?, is_featured = ?, is_breaking = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, news.getTitle());
            ps.setString(2, news.getSlug());
            ps.setString(3, news.getContent());
            ps.setString(4, news.getSummary());
            ps.setString(5, news.getImageUrl());
            ps.setLong(6, news.getCategoryId());
            ps.setString(7, news.getStatus());
            ps.setInt(8, news.isFeatured() ? 1 : 0);
            ps.setInt(9, news.isBreaking() ? 1 : 0);
            ps.setLong(10, news.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM news WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all news for admin (including drafts).
     */
    public PageInfo<News> findAll(int page, int pageSize) {
        List<News> list = new ArrayList<>();
        String countSql = "SELECT COUNT(*) FROM news";
        String dataSql = SELECT_JOIN + "ORDER BY n.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection()) {
            long total = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                if (rs.next()) total = rs.getLong(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                ps.setInt(1, pageSize);
                ps.setInt(2, (page - 1) * pageSize);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
            return new PageInfo<>(list, page, pageSize, total);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageInfo<>(list, page, pageSize, 0);
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM news WHERE status = 'PUBLISHED'";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long totalViews() {
        String sql = "SELECT COALESCE(SUM(view_count), 0) FROM news";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ========== Helpers ==========

    private boolean isLikedBy(Connection conn, Long newsId, Long userId) throws SQLException {
        String sql = "SELECT 1 FROM likes WHERE news_id = ? AND user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int setSearchParams(PreparedStatement ps, String keyword, String categorySlug) throws SQLException {
        int idx = 1;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(idx++, pattern);
            ps.setString(idx++, pattern);
            ps.setString(idx++, pattern);
        }
        if (categorySlug != null && !categorySlug.isEmpty()) {
            ps.setString(idx++, categorySlug);
        }
        return idx;
    }

    private News mapRow(ResultSet rs) throws SQLException {
        News news = new News();
        news.setId(rs.getLong("id"));
        news.setTitle(rs.getString("title"));
        news.setSlug(rs.getString("slug"));
        news.setContent(rs.getString("content"));
        news.setSummary(rs.getString("summary"));
        news.setImageUrl(rs.getString("image_url"));
        news.setCategoryId(rs.getLong("category_id"));
        news.setAuthorId(rs.getLong("author_id"));
        news.setStatus(rs.getString("status"));
        news.setViewCount(rs.getLong("view_count"));
        news.setFeatured(rs.getInt("is_featured") == 1);
        news.setBreaking(rs.getInt("is_breaking") == 1);
        news.setCreatedAt(rs.getTimestamp("created_at"));
        news.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Joined fields
        news.setCategoryName(rs.getString("category_name"));
        news.setCategorySlug(rs.getString("category_slug"));
        news.setCategoryColor(rs.getString("category_color"));
        news.setAuthorName(rs.getString("author_name"));
        news.setAuthorAvatar(rs.getString("author_avatar"));
        news.setCommentCount(rs.getInt("comment_count"));
        news.setLikeCount(rs.getInt("like_count"));

        return news;
    }
}
