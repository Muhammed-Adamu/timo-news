package com.news.model;

import java.sql.Timestamp;

public class User {
    private Long id;
    private String username;
    private String password; // hashed, never sent to client
    private String email;
    private String fullName;
    private String avatar;
    private String role;    // USER or ADMIN
    private String bio;
    private Timestamp createdAt;

    public User() {}

    public User(Long id, String username, String email, String fullName,
                String avatar, String role, String bio, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.avatar = avatar;
        this.role = role;
        this.bio = bio;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
