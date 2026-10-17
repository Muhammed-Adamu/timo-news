package com.news.dao;

import com.news.model.Contact;
import com.news.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    public boolean save(Contact contact) {
        String sql = "INSERT INTO contacts (name, email, subject, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, contact.getName());
            ps.setString(2, contact.getEmail());
            ps.setString(3, contact.getSubject());
            ps.setString(4, contact.getMessage());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) contact.setId(keys.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Contact> findAll() {
        List<Contact> list = new ArrayList<>();
        String sql = "SELECT * FROM contacts ORDER BY created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markAsRead(Long id) {
        String sql = "UPDATE contacts SET is_read = 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Contact mapRow(ResultSet rs) throws SQLException {
        Contact contact = new Contact();
        contact.setId(rs.getLong("id"));
        contact.setName(rs.getString("name"));
        contact.setEmail(rs.getString("email"));
        contact.setSubject(rs.getString("subject"));
        contact.setMessage(rs.getString("message"));
        contact.setRead(rs.getInt("is_read") == 1);
        contact.setCreatedAt(rs.getTimestamp("created_at"));
        return contact;
    }
}
