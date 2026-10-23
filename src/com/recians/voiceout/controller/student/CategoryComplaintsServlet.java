package com.recians.voiceout.controller.student;

import com.recians.voiceout.dao.ComplaintDAO;
import com.recians.voiceout.model.Category;
import com.recians.voiceout.model.Complaint;
import com.recians.voiceout.model.Student;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * CategoryComplaintsServlet - Displays existing public complaints for a specific category.
 * Provides support counts, 1-click endorsement, and enforces strict privacy for confidential issues.
 */
@WebServlet("/category/view")
public class CategoryComplaintsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ComplaintDAO complaintDAO;

    @Override
    public void init() {
        this.complaintDAO = new ComplaintDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // 1. Authenticate student session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/student/login");
            return;
        }

        Student student = (Student) session.getAttribute("studentUser");

        // 2. Read category ID
        String idStr = request.getParameter("id");
        int categoryId = 0;
        try {
            categoryId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        Category category = complaintDAO.getCategoryById(categoryId);
        if (category == null) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        request.setAttribute("category", category);

        // 3. Handle Confidential Category vs Public Categories
        if (category.isConfidential()) {
            request.setAttribute("isConfidentialCategory", true);
            request.setAttribute("complaints", Collections.emptyList());
        } else {
            List<Complaint> complaints = complaintDAO.getComplaintsByCategory(categoryId, student.getStudentId());
            request.setAttribute("isConfidentialCategory", false);
            request.setAttribute("complaints", complaints);
        }

        request.getRequestDispatcher("/student/category-complaints.jsp").forward(request, response);
    }
}
