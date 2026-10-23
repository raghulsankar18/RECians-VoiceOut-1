package com.recians.voiceout.controller.student;

import com.recians.voiceout.dao.ComplaintDAO;
import com.recians.voiceout.model.Student;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * SupportComplaintServlet - Handles 1-student-1-vote complaint endorsement.
 * Enforces server-side authentication and prevents duplicate support.
 */
@WebServlet("/complaint/support")
public class SupportComplaintServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ComplaintDAO complaintDAO;

    @Override
    public void init() {
        this.complaintDAO = new ComplaintDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // 1. Authenticate student session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/student/login");
            return;
        }

        Student student = (Student) session.getAttribute("studentUser");

        // 2. Parse complaintId and categoryId
        String complaintIdStr = request.getParameter("complaintId");
        String categoryIdStr = request.getParameter("categoryId");

        int complaintId = 0;
        int categoryId = 0;
        try {
            complaintId = Integer.parseInt(complaintIdStr);
            categoryId = Integer.parseInt(categoryIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        // 3. Register support (enforces non-confidential & non-duplicate in DAO)
        boolean success = complaintDAO.addSupport(complaintId, student.getStudentId());

        if (success) {
            response.sendRedirect(request.getContextPath() + "/category/view?id=" + categoryId + "&supported=true");
        } else {
            response.sendRedirect(request.getContextPath() + "/category/view?id=" + categoryId + "&error=already_supported");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/student/dashboard");
    }
}
