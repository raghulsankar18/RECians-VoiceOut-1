package com.recians.voiceout.controller.student;

import com.recians.voiceout.dao.ComplaintDAO;
import com.recians.voiceout.model.Category;
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
 * SubmitComplaintServlet - Handles student complaint submission (Stage 5).
 * 
 * Enforces server-side authentication, category validation across all 8 REC categories,
 * input length limits, and binds the complaint strictly to the session's student ID.
 */
@WebServlet("/complaint/submit")
public class SubmitComplaintServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ComplaintDAO complaintDAO;

    @Override
    public void init() {
        this.complaintDAO = new ComplaintDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // 1. Authorize student session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/student/login");
            return;
        }

        Student student = (Student) session.getAttribute("studentUser");
        if (!student.isProfileCompleted()) {
            response.sendRedirect(request.getContextPath() + "/student/profile");
            return;
        }

        // 2. Load 8 categories for the dropdown
        List<Category> categories = complaintDAO.getAllCategories();
        request.setAttribute("categories", categories);

        // Pre-select category if passed in query param (e.g. from dashboard card click)
        String preselectedCat = request.getParameter("categoryId");
        if (preselectedCat != null) {
            request.setAttribute("selectedCategoryId", preselectedCat);
        }

        request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Authorize student session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/student/login");
            return;
        }

        Student student = (Student) session.getAttribute("studentUser");
        // CRITICAL SECURITY: Never trust client-supplied student ID; extract from session
        int authenticatedStudentId = student.getStudentId();

        // 2. Read and sanitize form parameters
        String categoryIdStr = request.getParameter("categoryId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");

        List<Category> categories = complaintDAO.getAllCategories();
        request.setAttribute("categories", categories);

        // 3. Server-side validation
        int categoryId = 0;
        try {
            categoryId = Integer.parseInt(categoryIdStr);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Please select a valid complaint category.");
            request.setAttribute("title", title);
            request.setAttribute("description", description);
            request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
            return;
        }

        Category selectedCat = complaintDAO.getCategoryById(categoryId);
        if (selectedCat == null) {
            request.setAttribute("error", "Selected category does not exist.");
            request.setAttribute("title", title);
            request.setAttribute("description", description);
            request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
            return;
        }

        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            request.setAttribute("error", "Complaint title and description cannot be empty.");
            request.setAttribute("selectedCategoryId", categoryIdStr);
            request.setAttribute("title", title);
            request.setAttribute("description", description);
            request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
            return;
        }

        String cleanTitle = title.trim();
        String cleanDescription = description.trim();

        if (cleanTitle.length() < 5 || cleanTitle.length() > 150) {
            request.setAttribute("error", "Title must be between 5 and 150 characters.");
            request.setAttribute("selectedCategoryId", categoryIdStr);
            request.setAttribute("title", cleanTitle);
            request.setAttribute("description", cleanDescription);
            request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
            return;
        }

        if (cleanDescription.length() < 10 || cleanDescription.length() > 3000) {
            request.setAttribute("error", "Description must provide sufficient detail (between 10 and 3000 characters).");
            request.setAttribute("selectedCategoryId", categoryIdStr);
            request.setAttribute("title", cleanTitle);
            request.setAttribute("description", cleanDescription);
            request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
            return;
        }

        // 4. Save complaint using ComplaintDAO
        String generatedCode = complaintDAO.submitComplaint(authenticatedStudentId, categoryId, cleanTitle, cleanDescription);

        if (generatedCode != null) {
            request.setAttribute("successCode", generatedCode);
            request.setAttribute("submittedCategory", selectedCat.getCategoryName());
            request.setAttribute("isConfidential", selectedCat.isConfidential());
            request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Unable to save your complaint due to a temporary server issue. Please try again.");
            request.setAttribute("selectedCategoryId", categoryIdStr);
            request.setAttribute("title", cleanTitle);
            request.setAttribute("description", cleanDescription);
            request.getRequestDispatcher("/student/submit-complaint.jsp").forward(request, response);
        }
    }
}
