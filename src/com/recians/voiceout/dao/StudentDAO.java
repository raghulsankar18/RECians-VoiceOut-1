package com.recians.voiceout.dao;

import com.recians.voiceout.model.Student;
import com.recians.voiceout.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * StudentDAO - Database Access Object for Student operations.
 * 
 * Enforces college email domain validation (@rajalakshmi.edu.in),
 * uses PreparedStatement for all queries to prevent SQL injection,
 * and ensures passwords are encrypted via SHA-256.
 */
public class StudentDAO {

    public static final String COLLEGE_DOMAIN = "@rajalakshmi.edu.in";

    /**
     * Validates whether an email belongs to Rajalakshmi Engineering College.
     */
    public boolean isValidCollegeEmail(String email) {
        if (email == null) return false;
        String clean = email.trim().toLowerCase();
        return clean.endsWith(COLLEGE_DOMAIN) && clean.length() > COLLEGE_DOMAIN.length();
    }

    /**
     * Checks if an email is already registered in the system.
     */
    public boolean isEmailRegistered(String email) {
        if (email == null) return false;
        String sql = "SELECT 1 FROM students WHERE LOWER(email) = LOWER(?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email.trim());
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error checking email existence: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Registers a new student account after validating college domain and hashing password.
     */
    public boolean register(String fullName, String email, String plainPassword) {
        if (!isValidCollegeEmail(email)) {
            System.err.println("[StudentDAO] Rejected registration: Non-REC email domain.");
            return false;
        }
        if (fullName == null || fullName.trim().isEmpty() || plainPassword == null || plainPassword.length() < 6) {
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        String sql = "INSERT INTO students (full_name, email, password_hash, profile_completed) VALUES (?, ?, ?, FALSE)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, fullName.trim());
            pstmt.setString(2, email.trim().toLowerCase());
            pstmt.setString(3, passwordHash);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error registering student: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * Authenticates student credentials and returns a Student object.
     */
    public Student login(String email, String plainPassword) {
        if (email == null || plainPassword == null) {
            return null;
        }

        String sql = "SELECT student_id, full_name, email, register_number, department, academic_year, password_hash, profile_completed, created_at "
                   + "FROM students WHERE LOWER(email) = LOWER(?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email.trim());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("student_id"));
                    student.setFullName(rs.getString("full_name"));
                    student.setEmail(rs.getString("email"));
                    student.setRegisterNumber(rs.getString("register_number"));
                    student.setDepartment(rs.getString("department"));
                    student.setAcademicYear(rs.getString("academic_year"));
                    student.setPasswordHash(storedHash);
                    student.setProfileCompleted(rs.getBoolean("profile_completed"));
                    student.setCreatedAt(rs.getTimestamp("created_at"));
                    return student;
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error authenticating student: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Updates first-time student profile details (Register No, Department, Year).
     */
    public boolean updateProfile(int studentId, String regNo, String dept, String year) {
        String sql = "UPDATE students SET register_number = ?, department = ?, academic_year = ?, profile_completed = TRUE WHERE student_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, regNo != null ? regNo.trim() : null);
            pstmt.setString(2, dept != null ? dept.trim() : null);
            pstmt.setString(3, year != null ? year.trim() : null);
            pstmt.setInt(4, studentId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error updating student profile: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * Fetches a student by primary key ID.
     */
    public Student getStudentById(int studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Student s = new Student();
                s.setStudentId(rs.getInt("student_id"));
                s.setFullName(rs.getString("full_name"));
                s.setEmail(rs.getString("email"));
                s.setRegisterNumber(rs.getString("register_number"));
                s.setDepartment(rs.getString("department"));
                s.setAcademicYear(rs.getString("academic_year"));
                s.setProfileCompleted(rs.getBoolean("profile_completed"));
                s.setCreatedAt(rs.getTimestamp("created_at"));
                return s;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error fetching student by ID: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }
}
