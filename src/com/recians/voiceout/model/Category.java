package com.recians.voiceout.model;

import java.io.Serializable;

/**
 * Category JavaBean representing one of the 8 complaint categories at REC.
 */
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private int categoryId;
    private String categoryName;
    private String description;
    private boolean confidential;

    public Category() {}

    public Category(int categoryId, String categoryName, String description, boolean confidential) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.confidential = confidential;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }
}
