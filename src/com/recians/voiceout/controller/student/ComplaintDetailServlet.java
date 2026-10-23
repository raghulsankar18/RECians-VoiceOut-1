package com.recians.voiceout.controller.student;

import com.recians.voiceout.dao.ComplaintDAO;
import com.recians.voiceout.model.Complaint;
import com.recians.voiceout.model.Student;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ComplaintDetailServlet - Displays a single complaint's details for the owning student.
 * Ensures the complaint belongs to the logged-in student before rendering.
 */
@WebServlet("/student/complaint/view")
public class ComplaintDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ComplaintDAO complaintDAO;

    @Override
    public void init() {
        complaintDAO = new ComplaintDAO();
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
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }
        int complaintId;
        try {
            complaintId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }
        // Verify ownership via DAO method
        Complaint complaint = complaintDAO.getComplaintById(complaintId, student.getStudentId());
        if (complaint == null) {
            // Not found or not owned – forward with message
            request.setAttribute("errorMsg", "Complaint not found or you do not have permission to view it.");
            request.getRequestDispatcher("/student/dashboard.jsp").forward(request, response);
            return;
        }
        request.setAttribute("complaint", complaint);
        request.getRequestDispatcher("/student/complaintDetail.jsp").forward(request, response);
    }
}
