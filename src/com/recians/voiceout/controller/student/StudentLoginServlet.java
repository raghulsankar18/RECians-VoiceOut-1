package com.recians.voiceout.controller.student;

import com.recians.voiceout.dao.StudentDAO;
import com.recians.voiceout.model.Student;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * StudentLoginServlet - Handles student authentication and session establishment.
 * Directs new students to profile setup, and returning students to the dashboard.
 */
@WebServlet("/student/login")
public class StudentLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private StudentDAO studentDAO;

    @Override
    public void init() {
        this.studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Check if student is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("studentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        request.getRequestDispatcher("/student/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Please provide both college email and password.");
            request.getRequestDispatcher("/student/login.jsp").forward(request, response);
            return;
        }

        // Authenticate student
        Student student = studentDAO.login(email.trim(), password);

        if (student != null) {
            // Establish fresh, safe HTTP session
            HttpSession session = request.getSession(true);
            session.setAttribute("studentUser", student);
            session.setAttribute("studentId", student.getStudentId());
            session.setAttribute("studentName", student.getFullName());

            // Redirect based on profile status
            if (!student.isProfileCompleted()) {
                response.sendRedirect(request.getContextPath() + "/student/profile");
            } else {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
            }
        } else {
            request.setAttribute("error", "Invalid college email or password. Please try again.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/student/login.jsp").forward(request, response);
        }
    }
}
