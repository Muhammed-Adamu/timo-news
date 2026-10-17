package com.news.dao;

import com.news.model.Comment;
import com.news.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public List<Comment> findByNewsId(Long newsId) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.username, u.avatar AS user_avatar "
                + "FROM comments c LEFT JOIN users u ON c.user_id = u.id "
                + "WHERE c.news_id = ? AND c.status = 'APPROVED' "
                + "ORDER BY c.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
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

    public boolean create(Comment comment) {
        String sql = "INSERT INTO comments (news_id, user_id, content, status) VALUES (?, ?, ?, 'APPROVED')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, comment.getNewsId());
            ps.setLong(2, comment.getUserId());
            ps.setString(3, comment.getContent());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) comment.setId(keys.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Comment findById(Long id) {
        String sql = "SELECT c.*, u.username, u.avatar AS user_avatar "
                + "FROM comments c LEFT JOIN users u ON c.user_id = u.id WHERE c.id = ?";
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

    public boolean delete(Long id) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countByNewsId(Long newsId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE news_id = ? AND status = 'APPROVED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setNewsId(rs.getLong("news_id"));
        comment.setUserId(rs.getLong("user_id"));
        comment.setContent(rs.getString("content"));
        comment.setStatus(rs.getString("status"));
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        comment.setUsername(rs.getString("username"));
        comment.setUserAvatar(rs.getString("user_avatar"));
        return comment;
    }
}
