package com.news.dao;

import com.news.util.DBUtil;

import java.sql.*;
import java.util.List;

public class LikeDAO {

    /**
     * Toggle like: if exists, remove; if not exists, add.
     * Returns true if liked after toggle, false if unliked.
     */
    public boolean toggle(Long newsId, Long userId) {
        if (exists(newsId, userId)) {
            remove(newsId, userId);
            return false;
        } else {
            add(newsId, userId);
            return true;
        }
    }

    public boolean exists(Long newsId, Long userId) {
        String sql = "SELECT 1 FROM likes WHERE news_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countByNewsId(Long newsId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE news_id = ?";
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

    public List<Long> findLikedNewsIdsByUser(Long userId) {
        List<Long> ids = new java.util.ArrayList<>();
        String sql = "SELECT news_id FROM likes WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong("news_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    private void add(Long newsId, Long userId) {
        String sql = "INSERT INTO likes (news_id, user_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void remove(Long newsId, Long userId) {
        String sql = "DELETE FROM likes WHERE news_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newsId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
