package com.recians.voiceout.controller.student;

import com.recians.voiceout.dao.StudentDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * StudentRegisterServlet - Handles student sign-up.
 * Validates the strict @rajalakshmi.edu.in domain on the server side.
 */
@WebServlet("/student/register")
public class StudentRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private StudentDAO studentDAO;

    @Override
    public void init() {
        this.studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Forward to registration form
        request.getRequestDispatcher("/student/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Server-side validation
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/student/register.jsp").forward(request, response);
            return;
        }

        // 1. Strict REC college domain check
        if (!studentDAO.isValidCollegeEmail(email)) {
            request.setAttribute("error", "Registration is restricted to official college email addresses ending with @rajalakshmi.edu.in");
            request.setAttribute("fullName", fullName);
            request.getRequestDispatcher("/student/register.jsp").forward(request, response);
            return;
        }

        // 2. Password checks
        if (password.length() < 6) {
            request.setAttribute("error", "Password must be at least 6 characters long.");
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/student/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/student/register.jsp").forward(request, response);
            return;
        }

        // 3. Duplicate check
        if (studentDAO.isEmailRegistered(email)) {
            request.setAttribute("error", "This college email is already registered. Please log in.");
            request.getRequestDispatcher("/student/register.jsp").forward(request, response);
            return;
        }

        // 4. Save to database
        boolean success = studentDAO.register(fullName, email, password);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/student/login?registered=true");
        } else {
            request.setAttribute("error", "Registration could not be completed. Please try again.");
            request.getRequestDispatcher("/student/register.jsp").forward(request, response);
        }
    }
}
