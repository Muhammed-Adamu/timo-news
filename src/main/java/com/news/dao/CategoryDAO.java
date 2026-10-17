package com.news.dao;

import com.news.model.Category;
import com.news.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT c.*, (SELECT COUNT(*) FROM news n WHERE n.category_id = c.id AND n.status = 'PUBLISHED') AS news_count "
                + "FROM categories c ORDER BY c.sort_order";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category cat = mapRow(rs);
                cat.setNewsCount(rs.getInt("news_count"));
                list.add(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Category findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Category findBySlug(String slug) {
        String sql = "SELECT c.*, (SELECT COUNT(*) FROM news n WHERE n.category_id = c.id AND n.status = 'PUBLISHED') AS news_count "
                + "FROM categories c WHERE c.slug = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category cat = mapRow(rs);
                    cat.setNewsCount(rs.getInt("news_count"));
                    return cat;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Category category) {
        if (category.getId() == null) {
            return insert(category);
        } else {
            return update(category);
        }
    }

    private boolean insert(Category cat) {
        String sql = "INSERT INTO categories (name, slug, description, icon_class, color, sort_order) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cat.getName());
            ps.setString(2, cat.getSlug());
            ps.setString(3, cat.getDescription());
            ps.setString(4, cat.getIconClass());
            ps.setString(5, cat.getColor());
            ps.setInt(6, cat.getSortOrder());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) cat.setId(keys.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean update(Category cat) {
        String sql = "UPDATE categories SET name = ?, slug = ?, description = ?, icon_class = ?, "
                + "color = ?, sort_order = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cat.getName());
            ps.setString(2, cat.getSlug());
            ps.setString(3, cat.getDescription());
            ps.setString(4, cat.getIconClass());
            ps.setString(5, cat.getColor());
            ps.setInt(6, cat.getSortOrder());
            ps.setLong(7, cat.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category cat = new Category();
        cat.setId(rs.getLong("id"));
        cat.setName(rs.getString("name"));
        cat.setSlug(rs.getString("slug"));
        cat.setDescription(rs.getString("description"));
        cat.setIconClass(rs.getString("icon_class"));
        cat.setColor(rs.getString("color"));
        cat.setSortOrder(rs.getInt("sort_order"));
        cat.setCreatedAt(rs.getTimestamp("created_at"));
        return cat;
    }
}
