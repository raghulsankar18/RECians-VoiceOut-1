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
 * StudentProfileServlet - Manages first-time profile completion
 * for newly registered students (Register Number, Department, Year).
 */
@WebServlet("/student/profile")
public class StudentProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private StudentDAO studentDAO;

    @Override
    public void init() {
        this.studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/student/login");
            return;
        }

        Student student = (Student) session.getAttribute("studentUser");
        if (student.isProfileCompleted()) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        request.getRequestDispatcher("/student/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/student/login");
            return;
        }

        Student student = (Student) session.getAttribute("studentUser");

        String registerNumber = request.getParameter("registerNumber");
        String department = request.getParameter("department");
        String academicYear = request.getParameter("academicYear");

        if (registerNumber == null || registerNumber.trim().isEmpty() ||
            department == null || department.trim().isEmpty() ||
            academicYear == null || academicYear.trim().isEmpty()) {
            request.setAttribute("error", "Please fill in all profile details.");
            request.getRequestDispatcher("/student/profile.jsp").forward(request, response);
            return;
        }

        boolean ok = studentDAO.updateProfile(student.getStudentId(), registerNumber.trim(), department.trim(), academicYear.trim());
        if (ok) {
            student.setRegisterNumber(registerNumber.trim());
            student.setDepartment(department.trim());
            student.setAcademicYear(academicYear.trim());
            student.setProfileCompleted(true);
            session.setAttribute("studentUser", student);

            response.sendRedirect(request.getContextPath() + "/student/dashboard?profileUpdated=true");
        } else {
            request.setAttribute("error", "Failed to update profile. The Register Number may already be in use.");
            request.getRequestDispatcher("/student/profile.jsp").forward(request, response);
        }
    }
}
