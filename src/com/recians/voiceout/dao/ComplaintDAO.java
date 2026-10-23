package com.recians.voiceout.dao;

import com.recians.voiceout.model.Category;
import com.recians.voiceout.model.Complaint;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ComplaintDAO - Database Access Object for Complaints, Categories, and Community Support.
 * 
 * Handles complaint submission, unique ID generation (REC-YYYY-XXXXX),
 * category-specific queries, 1-student-1-vote support enforcement, and confidentiality filtering.
 */
public class ComplaintDAO {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Retrieves all 8 complaint categories ordered by category ID.
     */
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, description, is_confidential FROM categories ORDER BY category_id ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Category cat = new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getBoolean("is_confidential")
                );
                list.add(cat);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error retrieving categories: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Retrieves a single category by its primary key ID.
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT category_id, category_name, description, is_confidential FROM categories WHERE category_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getBoolean("is_confidential")
                );
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error finding category: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * Generates a unique, collision-resistant complaint tracking code.
     * Example: REC-2026-73921
     */
    public String generateUniqueComplaintCode() {
        int currentYear = Year.now().getValue();
        for (int attempts = 0; attempts < 10; attempts++) {
            int randomNum = 10000 + RANDOM.nextInt(90000); // 5 digits (10000-99999)
            String candidateCode = "REC-" + currentYear + "-" + randomNum;

            if (!isCodeExists(candidateCode)) {
                return candidateCode;
            }
        }
        return "REC-" + currentYear + "-" + (System.currentTimeMillis() % 100000);
    }

    private boolean isCodeExists(String code) {
        String sql = "SELECT 1 FROM complaints WHERE complaint_code = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Submits a new grievance.
     */
    public String submitComplaint(int studentId, int categoryId, String title, String description) {
        if (studentId <= 0 || title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            return null;
        }

        Category cat = getCategoryById(categoryId);
        if (cat == null) {
            System.err.println("[ComplaintDAO] Invalid category ID: " + categoryId);
            return null;
        }

        boolean isConfidential = cat.isConfidential();
        String complaintCode = generateUniqueComplaintCode();

        String sql = "INSERT INTO complaints (complaint_code, student_id, category_id, title, description, status, is_confidential, support_count) "
                   + "VALUES (?, ?, ?, ?, ?, 'Pending', ?, 0)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, complaintCode);
            pstmt.setInt(2, studentId);
            pstmt.setInt(3, categoryId);
            pstmt.setString(4, title.trim());
            pstmt.setString(5, description.trim());
            pstmt.setBoolean(6, isConfidential);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                return complaintCode;
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error inserting complaint: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt);
        }
        return null;
    }

    /**
     * Retrieves all public, non-confidential complaints belonging to a specific category.
     * Also detects whether the viewing student has already supported each complaint.
     */
    public List<Complaint> getComplaintsByCategory(int categoryId, int currentStudentId) {
        List<Complaint> list = new ArrayList<>();

        // If category is confidential, never return any public complaints!
        Category cat = getCategoryById(categoryId);
        if (cat == null || cat.isConfidential()) {
            return list;
        }

        // 1. Fetch complaints for this category (excluding confidential)
        String sql = "SELECT c.*, cat.category_name "
                   + "FROM complaints c "
                   + "JOIN categories cat ON c.category_id = cat.category_id "
                   + "WHERE c.category_id = ? AND c.is_confidential = FALSE "
                   + "ORDER BY c.support_count DESC, c.created_at DESC";

        // 2. Fetch supported IDs by this student
        Set<Integer> supportedIds = getSupportedComplaintIds(currentStudentId);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Complaint c = new Complaint();
                int compId = rs.getInt("complaint_id");
                c.setComplaintId(compId);
                c.setComplaintCode(rs.getString("complaint_code"));
                c.setStudentId(rs.getInt("student_id"));
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setStatus(rs.getString("status"));
                c.setConfidential(false);
                c.setSupportCount(rs.getInt("support_count"));
                c.setAdminRemarks(rs.getString("admin_remarks"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setResolvedAt(rs.getTimestamp("resolved_at"));
                c.setSupportedByCurrentUser(supportedIds.contains(compId));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error fetching category complaints: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Checks if a student has already supported a given complaint.
     */
    public boolean hasStudentSupported(int complaintId, int studentId) {
        String sql = "SELECT 1 FROM complaint_supports WHERE complaint_id = ? AND student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, complaintId);
            pstmt.setInt(2, studentId);
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Returns a set of complaint IDs that the given student has supported.
     */
    public Set<Integer> getSupportedComplaintIds(int studentId) {
        Set<Integer> set = new HashSet<>();
        if (studentId <= 0) return set;

        String sql = "SELECT complaint_id FROM complaint_supports WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getInt("complaint_id"));
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error getting supported complaint IDs: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return set;
    }

    /**
     * Adds 1-student-1-vote support to a public complaint.
     * Enforces that confidential complaints CANNOT be supported.
     * Enforces duplicate support prevention via database transaction and unique constraints.
     * 
     * @return true if support was successfully registered and count incremented; false otherwise.
     */
    public boolean addSupport(int complaintId, int studentId) {
        if (complaintId <= 0 || studentId <= 0) {
            return false;
        }

        String checkSql = "SELECT is_confidential FROM complaints WHERE complaint_id = ?";
        String insertSql = "INSERT INTO complaint_supports (complaint_id, student_id) VALUES (?, ?)";
        String updateSql = "UPDATE complaints SET support_count = support_count + 1 WHERE complaint_id = ? AND is_confidential = FALSE";

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            // 1. Verify complaint is NOT confidential
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, complaintId);
            rs = checkStmt.executeQuery();
            if (rs.next()) {
                boolean isConf = rs.getBoolean("is_confidential");
                if (isConf) {
                    System.err.println("[ComplaintDAO] Rejected support: Complaint is Private & Confidential.");
                    conn.rollback();
                    return false;
                }
            } else {
                conn.rollback();
                return false;
            }

            // 2. Insert vote into complaint_supports (unique constraint prevents duplicates)
            insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, complaintId);
            insertStmt.setInt(2, studentId);
            insertStmt.executeUpdate();

            // 3. Atomically increment support_count in complaints table
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, complaintId);
            updateStmt.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[ComplaintDAO] Support rejected (likely duplicate or error): " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(null, checkStmt, rs);
            DBConnection.close(null, insertStmt);
            DBConnection.close(conn, updateStmt);
        }
    }

    /**
     * Retrieves all complaints filed by a specific student.
     */
    public List<Complaint> getComplaintsByStudent(int studentId) {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.*, cat.category_name "
                   + "FROM complaints c "
                   + "JOIN categories cat ON c.category_id = cat.category_id "
                   + "WHERE c.student_id = ? "
                   + "ORDER BY c.created_at DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Complaint c = new Complaint();
                c.setComplaintId(rs.getInt("complaint_id"));
                c.setComplaintCode(rs.getString("complaint_code"));
                c.setStudentId(rs.getInt("student_id"));
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setStatus(rs.getString("status"));
                c.setConfidential(rs.getBoolean("is_confidential"));
                c.setSupportCount(rs.getInt("support_count"));
                c.setAdminRemarks(rs.getString("admin_remarks"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setResolvedAt(rs.getTimestamp("resolved_at"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error fetching student complaints: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Retrieves a specific complaint ensuring it belongs to the given student.
     * Returns null if not found or not owned.
     */
    public Complaint getComplaintById(int complaintId, int ownerStudentId) {
        String sql = "SELECT c.*, cat.category_name FROM complaints c "
                   + "JOIN categories cat ON c.category_id = cat.category_id "
                   + "WHERE c.complaint_id = ? AND c.student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, complaintId);
            pstmt.setInt(2, ownerStudentId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Complaint c = new Complaint();
                c.setComplaintId(rs.getInt("complaint_id"));
                c.setComplaintCode(rs.getString("complaint_code"));
                c.setStudentId(rs.getInt("student_id"));
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setStatus(rs.getString("status"));
                c.setConfidential(rs.getBoolean("is_confidential"));
                c.setSupportCount(rs.getInt("support_count"));
                c.setAdminRemarks(rs.getString("admin_remarks"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setResolvedAt(rs.getTimestamp("resolved_at"));
                return c;
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error fetching complaint by ID: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * Retrieves public complaints from the secure public_complaints_view.
     */
    public List<Complaint> getPublicComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT * FROM public_complaints_view ORDER BY created_at DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Complaint c = new Complaint();
                c.setComplaintId(rs.getInt("complaint_id"));
                c.setComplaintCode(rs.getString("complaint_code"));
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setStatus(rs.getString("status"));
                c.setSupportCount(rs.getInt("support_count"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setConfidential(false);
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error fetching public complaints: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }
}
