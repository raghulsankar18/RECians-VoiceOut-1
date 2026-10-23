package com.recians.voiceout.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Complaint JavaBean representing a grievance submitted by an REC student.
 */
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    private int complaintId;
    private String complaintCode;
    private int studentId;
    private int categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String status;
    private boolean confidential;
    private int supportCount;
    private String adminRemarks;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resolvedAt;
    private boolean supportedByCurrentUser;

    public Complaint() {
        this.status = "Pending";
        this.supportCount = 0;
        this.confidential = false;
        this.supportedByCurrentUser = false;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintCode() {
        return complaintCode;
    }

    public void setComplaintCode(String complaintCode) {
        this.complaintCode = complaintCode;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public int getSupportCount() {
        return supportCount;
    }

    public void setSupportCount(int supportCount) {
        this.supportCount = supportCount;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public boolean isSupportedByCurrentUser() {
        return supportedByCurrentUser;
    }

    public void setSupportedByCurrentUser(boolean supportedByCurrentUser) {
        this.supportedByCurrentUser = supportedByCurrentUser;
    }
}
