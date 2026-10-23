<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.recians.voiceout.model.Student" %>
<%
    Student currentStudent = (Student) session.getAttribute("studentUser");
    if (currentStudent == null) {
        response.sendRedirect(request.getContextPath() + "/student/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Complete Student Profile - RECian's VoiceOut</title>
    <link rel="stylesheet" href="../css/style.css">
</head>
<body>
    <header>
        <div class="logo-area">
            <span class="badge">REC</span>
            <div>
                <div class="title-text">RECian's VoiceOut</div>
                <div class="sub-tagline">Rajalakshmi Engineering College &bull; Student Onboarding</div>
            </div>
        </div>
        <div>
            <a href="<%= request.getContextPath() %>/student/logout" class="btn btn-secondary" style="padding: 0.5rem 1rem; font-size: 0.85rem;">Sign Out</a>
        </div>
    </header>

    <main class="container">
        <div class="auth-card">
            <h2>Welcome, <%= currentStudent.getFullName() %>!</h2>
            <p class="subtitle">Please complete your academic profile to unlock the complaint portal</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <form action="<%= request.getContextPath() %>/student/profile" method="post">
                <div class="form-group">
                    <label for="registerNumber">College Register Number <span class="required">*</span></label>
                    <input type="text" id="registerNumber" name="registerNumber" 
                           placeholder="Enter your Register Number" 
                           required>
                </div>

                <div class="form-group">
                    <label for="department">Department <span class="required">*</span></label>
                    <select id="department" name="department" required>
                        <option value="">-- Select Department --</option>
                        <option value="Computer Science & Engineering (CSE)">Computer Science & Engineering (CSE)</option>
                        <option value="Electronics & Communication Engineering (ECE)">Electronics & Communication Engineering (ECE)</option>
                        <option value="Electrical & Electronics Engineering (EEE)">Electrical & Electronics Engineering (EEE)</option>
                        <option value="Mechanical Engineering">Mechanical Engineering</option>
                        <option value="Civil Engineering">Civil Engineering</option>
                        <option value="Information Technology (IT)">Information Technology (IT)</option>
                        <option value="Artificial Intelligence & Machine Learning (AI & ML)">Artificial Intelligence & Machine Learning (AI & ML)</option>
                        <option value="Artificial Intelligence & Data Science (AI & DS)">Artificial Intelligence & Data Science (AI & DS)</option>
                        <option value="Cyber Security">Cyber Security</option>
                        <option value="Computer Science and Design">Computer Science and Design</option>
                        <option value="Computer Science and Business Systems">Computer Science and Business Systems</option>
                        <option value="Robotics and Automation">Robotics and Automation</option>
                        <option value="Mechatronics">Mechatronics</option>
                        <option value="Aeronautical Engineering">Aeronautical Engineering</option>
                        <option value="Automobile Engineering">Automobile Engineering</option>
                        <option value="Biomedical Engineering">Biomedical Engineering</option>
                        <option value="Biotechnology">Biotechnology</option>
                        <option value="Chemical Engineering">Chemical Engineering</option>
                        <option value="Food Technology">Food Technology</option>
                        <option value="B.Arch (Bachelor of Architecture)">B.Arch (Bachelor of Architecture)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="academicYear">Current Academic Year <span class="required">*</span></label>
                    <select id="academicYear" name="academicYear" required>
                        <option value="">-- Select Year --</option>
                        <option value="1st Year">1st Year</option>
                        <option value="2nd Year">2nd Year</option>
                        <option value="3rd Year">3rd Year</option>
                        <option value="4th Year">4th Year</option>
                    </select>
                </div>

                <button type="submit" class="btn btn-primary btn-block">Save &amp; Continue to Dashboard &rarr;</button>
            </form>
        </div>
    </main>

    <footer>
        &copy; <%= java.time.Year.now() %> Rajalakshmi Engineering College &bull; RECian's VoiceOut
    </footer>
</body>
</html>
