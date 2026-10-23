package com.recians.voiceout.controller.student;

import com.recians.voiceout.dao.ComplaintDAO;
import com.recians.voiceout.model.Category;
import com.recians.voiceout.model.Complaint;
import com.recians.voiceout.model.Student;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * StudentDashboardServlet - Displays student dashboard with personalized greeting,
 * categories, quick stats, and link to submit grievances.
 */
@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ComplaintDAO complaintDAO;

    @Override
    public void init() {
        this.complaintDAO = new ComplaintDAO();
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

        // If profile is not yet completed, redirect to complete profile
        if (!student.isProfileCompleted()) {
            response.sendRedirect(request.getContextPath() + "/student/profile");
            return;
        }

        // Load 8 categories
        List<Category> categories = complaintDAO.getAllCategories();
        // Load student's own complaints
        List<Complaint> myComplaints = complaintDAO.getComplaintsByStudent(student.getStudentId());

        request.setAttribute("categories", categories);
        request.setAttribute("myComplaints", myComplaints);

        request.getRequestDispatcher("/student/dashboard.jsp").forward(request, response);
    }
}
