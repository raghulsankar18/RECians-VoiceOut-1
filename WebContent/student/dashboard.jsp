<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.recians.voiceout.model.Student" %>
<%@ page import="com.recians.voiceout.model.Category" %>
<%@ page import="com.recians.voiceout.model.Complaint" %>
<%@ page import="java.util.List" %>
<%
    Student student = (Student) session.getAttribute("studentUser");
    if (student == null) {
        response.sendRedirect(request.getContextPath() + "/student/login");
        return;
    }
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    List<Complaint> myComplaints = (List<Complaint>) request.getAttribute("myComplaints");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard - RECian's VoiceOut</title>
    <link rel="stylesheet" href="../css/style.css">
</head>
<body>
    <header>
        <div class="logo-area">
            <span class="badge">REC</span>
            <div>
                <div class="title-text">RECian's VoiceOut</div>
                <div class="sub-tagline">Rajalakshmi Engineering College &bull; Student Portal</div>
            </div>
        </div>
        <div style="display:flex; align-items:center; gap:1rem;">
            <span style="font-size:0.9rem; color:#94a3b8;">
                <%= student.getFullName() %> (<%= student.getDepartment() != null ? student.getDepartment() : "" %>)
            </span>
            <a href="<%= request.getContextPath() %>/student/logout" class="btn btn-secondary" style="padding:0.4rem 0.8rem; font-size:0.8rem;">Logout</a>
        </div>
    </header>

    <main class="container">
        <!-- Hero Greeting & Separate Complaint Submission CTA -->
        <div class="dashboard-greeting">
            <div>
                <h2>Hello, <%= student.getFullName() %>!</h2>
                <p>Welcome to your central campus grievance and feedback redressal platform.</p>
            </div>
            <!-- Separate Submit Complaint Button (Preserved & Prominent) -->
            <div>
                <a href="<%= request.getContextPath() %>/complaint/submit" class="btn btn-primary btn-large submit-cta-btn">
                    &#43; Submit Complaint
                </a>
            </div>
        </div>

        <% if ("true".equals(request.getParameter("profileUpdated"))) { %>
            <div class="alert alert-success">
                Academic profile completed successfully! You can now submit and track complaints.
            </div>
        <% } %>

        <!-- Horizontal Scrolling Categories Section -->
        <div class="carousel-header-area">
            <div>
                <h3 class="section-heading">Campus Sectors &bull; Categories</h3>
                <p class="section-subheading">Scroll horizontally to explore categories. Click any sector to view and support community complaints.</p>
            </div>
            <div class="carousel-nav-hint">
                <span class="scroll-arrow">&larr;</span>
                <span>Scroll Categories</span>
                <span class="scroll-arrow">&rarr;</span>
            </div>
        </div>

        <div class="category-carousel-wrapper">
            <div class="category-carousel" id="categoryCarousel">
                <% if (categories != null) {
                    for (Category cat : categories) { 
                        boolean isConf = cat.isConfidential();
                %>
                    <a href="<%= request.getContextPath() %>/category/view?id=<%= cat.getCategoryId() %>" 
                       class="carousel-card <%= isConf ? "category-confidential" : "" %>">
                        <div class="carousel-card-top">
                            <span class="category-index">Sector 0<%= cat.getCategoryId() %></span>
                            <% if (isConf) { %>
                                <span class="badge-confidential">&#128274; Confidential</span>
                            <% } %>
                        </div>
                        <h4><%= cat.getCategoryName() %></h4>
                        <p><%= cat.getDescription() %></p>
                        <span class="card-action">View Grievances &amp; Support &rarr;</span>
                    </a>
                <%  } 
                   } %>
            </div>
        </div>

        <!-- Recent Personal Grievances -->
        <div class="recent-section" style="margin-top:2.5rem;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
                <div>
                    <h3 class="section-heading" style="margin:0;">My Grievances &amp; Status Tracking</h3>
                    <p class="section-subheading" style="margin:0;">Monitor progress updates on complaints you submitted</p>
                </div>
                <a href="<%= request.getContextPath() %>/complaint/submit" class="link-highlight" style="font-size:0.9rem;">
                    &#43; Submit Another Issue
                </a>
            </div>

            <% if (myComplaints == null || myComplaints.isEmpty()) { %>
                <div class="empty-state">
                    <p style="font-size:1.05rem; margin-bottom:0.5rem;">You haven't submitted any complaints yet.</p>
                    <p style="color:#64748b; font-size:0.9rem;">
                        Click <strong>"+ Submit Complaint"</strong> above to raise a problem to the college administration.
                    </p>
                </div>
            <% } else { %>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Tracking Code</th>
                                <th>Category</th>
                                <th>Subject</th>
                                <th>Date Filed</th>
                                <th>Support Count</th>
                                <th>Status</th>
                                <th>Action</th>
                                <th>Visibility</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Complaint c : myComplaints) { %>
                                <tr>
                                    <td><strong style="color:#38bdf8;"><%= c.getComplaintCode() %></strong></td>
                                    <td><%= c.getCategoryName() %></td>
                                    <td><%= c.getTitle() %></td>
                                    <td><%= c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 10) : "" %></td>
                                    <td><%= c.getSupportCount() %> student(s)</td>
                                    <td>
                                        <span class="status-badge status-<%= c.getStatus().toLowerCase().replace(" ", "-") %>">
                                            <%= c.getStatus() %>
                                        </span>
                                    </td>
                                    <td><a href="${pageContext.request.contextPath}/complaint/view?id=${c.getComplaintId()}" class="btn btn-sm btn-outline-primary">View</a></td>
                                    <td>
                                        <% if (c.isConfidential()) { %>
                                            <span class="badge-confidential">&#128274; Private</span>
                                        <% } else { %>
                                            <span style="color:#94a3b8; font-size:0.85rem;">Public</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </div>
    </main>

    <footer>
        &copy; <%= java.time.Year.now() %> Rajalakshmi Engineering College &bull; RECian's VoiceOut
    </footer>
</body>
</html>
