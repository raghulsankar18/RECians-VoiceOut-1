<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.recians.voiceout.model.Student" %>
<%@ page import="com.recians.voiceout.model.Category" %>
<%@ page import="com.recians.voiceout.model.Complaint" %>
<%@ page import="java.util.List" %>
<%
    Student currentStudent = (Student) session.getAttribute("studentUser");
    if (currentStudent == null) {
        response.sendRedirect(request.getContextPath() + "/student/login");
        return;
    }
    Category category = (Category) request.getAttribute("category");
    List<Complaint> complaints = (List<Complaint>) request.getAttribute("complaints");
    boolean isConfidentialCategory = Boolean.TRUE.equals(request.getAttribute("isConfidentialCategory"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= category != null ? category.getCategoryName() : "Category" %> - RECian's VoiceOut</title>
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
                <div class="sub-tagline">Rajalakshmi Engineering College &bull; Category Issues</div>
            </div>
        </div>
        <div style="display:flex; align-items:center; gap:1rem;">
            <a href="<%= request.getContextPath() %>/student/dashboard" class="btn btn-secondary" style="padding:0.5rem 1rem; font-size:0.85rem;">
                &larr; Back to Dashboard
            </a>
            <a href="<%= request.getContextPath() %>/student/logout" class="btn btn-secondary" style="padding:0.5rem 0.8rem; font-size:0.85rem;">
                Logout
            </a>
        </div>
    </header>

    <main class="container">
        <!-- Category Banner -->
        <div class="category-banner <%= isConfidentialCategory ? "category-banner-confidential" : "" %>">
            <div class="category-banner-header">
                <div>
                    <span class="hero-tag" style="margin-bottom:0.5rem;">Category Overview</span>
                    <h2><%= category.getCategoryName() %></h2>
                    <p><%= category.getDescription() %></p>
                </div>
                <% if (isConfidentialCategory) { %>
                    <div>
                        <span class="badge-confidential" style="font-size:0.9rem; padding:0.4rem 0.8rem;">
                            &#128274; Strictly Confidential Sector
                        </span>
                    </div>
                <% } %>
            </div>
        </div>

        <!-- Feedback Notifications -->
        <% if ("true".equals(request.getParameter("supported"))) { %>
            <div class="alert alert-success">
                &#10004; <strong>Thank you!</strong> Your support has been recorded for this campus grievance.
            </div>
        <% } %>

        <% if ("already_supported".equals(request.getParameter("error"))) { %>
            <div class="alert alert-notice">
                &#9888; <strong>Notice:</strong> You have already supported this complaint. Each student can support a complaint once.
            </div>
        <% } %>

        <% if (isConfidentialCategory) { %>
            <!-- Confidential Sector Notice -->
            <div class="confidential-protection-card">
                <div style="font-size:2.5rem; margin-bottom:1rem;">&#128274;</div>
                <h3>Confidentiality Guaranteed</h3>
                <p>
                    Grievances filed under <strong>Private &amp; Confidential</strong> (such as sensitive individual concerns,
                    harassment, or ragging) are protected by strict privacy controls.
                </p>
                <p style="color:#cbd5e1; margin-top:0.5rem;">
                    These records <strong>never appear in public lists</strong> and cannot receive public support.
                    They are accessible only by authorized college administrators and the individual student who submitted them.
                </p>
                <div style="margin-top:1.5rem;">
                    <a href="<%= request.getContextPath() %>/student/dashboard" class="btn btn-secondary">
                        &larr; Return to Dashboard
                    </a>
                </div>
            </div>

        <% } else { %>

            <!-- Public Complaints List for this Category -->
            <div class="category-complaints-section">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem;">
                    <div>
                        <h3 class="section-heading" style="margin:0;">Active Campus Grievances</h3>
                        <p class="section-subheading" style="margin:0;">Review reported issues and support concerns that affect you</p>
                    </div>
                    <div style="color:#94a3b8; font-size:0.9rem;">
                        <%= complaints != null ? complaints.size() : 0 %> issue(s) reported
                    </div>
                </div>

                <% if (complaints == null || complaints.isEmpty()) { %>
                    <div class="empty-state">
                        <p style="font-size:1.05rem; margin-bottom:0.5rem;">No complaints yet in this category.</p>
                        <p style="color:#64748b; font-size:0.9rem;">
                            There are currently no open public grievances filed under <%= category.getCategoryName() %>.
                        </p>
                    </div>
                <% } else { %>
                    <div class="complaint-card-list">
                        <% for (Complaint c : complaints) { %>
                            <div class="complaint-card">
                                <div class="complaint-card-top">
                                    <div class="complaint-meta">
                                        <span class="complaint-code-tag"><%= c.getComplaintCode() %></span>
                                        <span class="complaint-date">Filed on <%= c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 10) : "" %></span>
                                    </div>
                                    <div>
                                        <span class="status-badge status-<%= c.getStatus().toLowerCase().replace(" ", "-") %>">
                                            <%= c.getStatus() %>
                                        </span>
                                    </div>
                                </div>

                                <h4 class="complaint-title"><%= c.getTitle() %></h4>
                                <p class="complaint-desc"><%= c.getDescription() %></p>

                                <div class="complaint-card-footer">
                                    <div class="support-count-display">
                                        <span class="support-icon">&#128101;</span>
                                        <strong><%= c.getSupportCount() %></strong>
                                        <span>student(s) supported this issue</span>
                                    </div>

                                    <div class="support-action">
                                        <% if (c.isSupportedByCurrentUser()) { %>
                                            <span class="badge-supported">
                                                &#10004; Supported by You
                                            </span>
                                        <% } else { %>
                                            <form action="<%= request.getContextPath() %>/complaint/support" method="post" style="margin:0;">
                                                <input type="hidden" name="complaintId" value="<%= c.getComplaintId() %>">
                                                <input type="hidden" name="categoryId" value="<%= category.getCategoryId() %>">
                                                <button type="submit" class="btn btn-support">
                                                    &#128077; Support Issue
                                                </button>
                                            </form>
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            </div>
        <% } %>
    </main>

    <footer>
        &copy; <%= java.time.Year.now() %> Rajalakshmi Engineering College &bull; RECian's VoiceOut
    </footer>
</body>
</html>
