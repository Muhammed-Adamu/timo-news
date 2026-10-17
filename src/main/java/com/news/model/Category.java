package com.news.model;

import java.sql.Timestamp;

public class Category {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconClass;
    private String color;
    private int sortOrder;
    private Timestamp createdAt;
    private int newsCount; // transient, for display

    public Category() {}

    public Category(Long id, String name, String slug, String description,
                    String iconClass, String color, int sortOrder, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.iconClass = iconClass;
        this.color = color;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getNewsCount() { return newsCount; }
    public void setNewsCount(int newsCount) { this.newsCount = newsCount; }
}
