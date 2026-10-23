<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.recians.voiceout.model.Student" %>
<%@ page import="com.recians.voiceout.model.Category" %>
<%@ page import="java.util.List" %>
<%
    Student currentStudent = (Student) session.getAttribute("studentUser");
    if (currentStudent == null) {
        response.sendRedirect(request.getContextPath() + "/student/login");
        return;
    }
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    String selectedCatId = (String) request.getAttribute("selectedCategoryId");
    String successCode = (String) request.getAttribute("successCode");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Submit Complaint - RECian's VoiceOut</title>
    <link rel="stylesheet" href="../css/style.css">
</head>
<body>
    <header>
        <div class="logo-area">
            <span class="badge">REC</span>
            <div>
                <a href="<%= request.getContextPath() %>/student/dashboard" style="text-decoration:none; color:inherit;">
                    <div class="title-text">RECian's VoiceOut</div>
                </a>
                <div class="sub-tagline">Rajalakshmi Engineering College &bull; Grievance Submission</div>
            </div>
        </div>
        <div style="display:flex; align-items:center; gap:1rem;">
            <a href="<%= request.getContextPath() %>/student/dashboard" class="btn btn-secondary" style="padding: 0.5rem 1rem; font-size: 0.85rem;">
                &larr; Dashboard
            </a>
            <a href="<%= request.getContextPath() %>/student/logout" class="btn btn-secondary" style="padding: 0.5rem 0.8rem; font-size: 0.85rem;">
                Logout
            </a>
        </div>
    </header>

    <main class="container">
        <% if (successCode != null) { %>
            <!-- Stage 5 Submission Success Confirmation Card -->
            <div class="confirmation-card">
                <div class="confirmation-icon">&#10004;</div>
                <h2>Grievance Submitted Successfully!</h2>
                <p>Your concern has been registered and forwarded to the college administration for review.</p>

                <div class="tracking-box">
                    <div class="tracking-label">Official Complaint Reference Code</div>
                    <div class="tracking-code"><%= successCode %></div>
                    <div class="tracking-status">
                        Initial Status: <span class="status-badge status-pending">Pending</span>
                    </div>
                </div>

                <% if (Boolean.TRUE.equals(request.getAttribute("isConfidential"))) { %>
                    <div class="alert alert-confidential">
                        &#128274; <strong>Confidentiality Protection Active:</strong> This complaint is categorized as 
                        <strong>Private &amp; Confidential</strong>. It will NEVER appear on any public board, cannot receive public support, 
                        and can only be accessed by authorized college administrators.
                    </div>
                <% } else { %>
                    <div class="alert alert-info">
                        This is an ordinary college grievance. Once reviewed, it may be listed under public complaints 
                        so fellow students can support the resolution.
                    </div>
                <% } %>

                <div class="btn-group" style="margin-top:2rem;">
                    <a href="<%= request.getContextPath() %>/student/dashboard" class="btn btn-primary">
                        Return to Dashboard
                    </a>
                    <a href="<%= request.getContextPath() %>/complaint/submit" class="btn btn-secondary">
                        Submit Another Complaint
                    </a>
                </div>
            </div>

        <% } else { %>

            <!-- Complaint Form Card -->
            <div class="form-container">
                <div class="form-header">
                    <h2>Submit a Campus Grievance or Concern</h2>
                    <p class="subtitle">Please provide accurate and constructive details to ensure quick action by REC administration.</p>
                </div>

                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert alert-error">
                        <%= request.getAttribute("error") %>
                    </div>
                <% } %>

                <form action="<%= request.getContextPath() %>/complaint/submit" method="post">
                    <!-- Complainant Identity Confirmation -->
                    <div class="user-strip">
                        <div>
                            <strong>Complainant:</strong> <%= currentStudent.getFullName() %> &bull; 
                            <%= currentStudent.getEmail() %> &bull; 
                            Dept: <%= currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "Not specified" %>
                        </div>
                        <small style="color:#94a3b8;">Linked automatically to your student session</small>
                    </div>

                    <!-- Category Selection -->
                    <div class="form-group">
                        <label for="categoryId">Complaint Category <span class="required">*</span></label>
                        <select id="categoryId" name="categoryId" required>
                            <option value="">-- Choose from the 8 official categories --</option>
                            <% if (categories != null) {
                                for (Category cat : categories) { 
                                    boolean isSelected = selectedCatId != null && selectedCatId.equals(String.valueOf(cat.getCategoryId()));
                                    String label = cat.getCategoryName() + (cat.isConfidential() ? " (Confidential)" : "");
                            %>
                                <option value="<%= cat.getCategoryId() %>" <%= isSelected ? "selected" : "" %>>
                                    <%= label %>
                                </option>
                            <%  }
                               } %>
                        </select>
                        <small class="form-hint">Selecting "Private &amp; Confidential" guarantees strict privacy from other students.</small>
                    </div>

                    <!-- Complaint Title -->
                    <div class="form-group">
                        <label for="title">Complaint Subject / Title <span class="required">*</span></label>
                        <input type="text" id="title" name="title" 
                               placeholder="e.g. Water shortage in Hostel Block B 3rd Floor" 
                               maxlength="150" 
                               minlength="5"
                               value="<%= request.getAttribute("title") != null ? request.getAttribute("title") : "" %>"
                               required>
                        <small class="form-hint">A concise summary of the issue (5 - 150 characters).</small>
                    </div>

                    <!-- Complaint Description -->
                    <div class="form-group">
                        <label for="description">Detailed Description <span class="required">*</span></label>
                        <textarea id="description" name="description" rows="7" 
                                  placeholder="Provide relevant details: location, date/time noticed, specific concern, and any suggested resolution..." 
                                  minlength="10" 
                                  maxlength="3000" 
                                  required><%= request.getAttribute("description") != null ? request.getAttribute("description") : "" %></textarea>
                        <small class="form-hint">Provide constructive and specific information (10 - 3000 characters).</small>
                    </div>

                    <!-- Submission Notice -->
                    <div class="alert alert-notice">
                        <strong>Note:</strong> By submitting, your grievance will be assigned a unique tracking code (REC-2026-XXXXX) 
                        with initial status <strong>Pending</strong>. You can monitor progress on your student dashboard.
                    </div>

                    <div style="display:flex; justify-content:space-between; align-items:center; margin-top:2rem;">
                        <a href="<%= request.getContextPath() %>/student/dashboard" class="btn btn-secondary">
                            Cancel
                        </a>
                        <button type="submit" class="btn btn-primary btn-large">
                            Submit Complaint &rarr;
                        </button>
                    </div>
                </form>
            </div>
        <% } %>
    </main>

    <footer>
        &copy; <%= java.time.Year.now() %> Rajalakshmi Engineering College &bull; RECian's VoiceOut
    </footer>
</body>
</html>
