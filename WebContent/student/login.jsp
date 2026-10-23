<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Sign In - RECian's VoiceOut</title>
    <link rel="stylesheet" href="../css/style.css">
</head>
<body>
    <header>
        <div class="logo-area">
            <span class="badge">REC</span>
            <div>
                <a href="../index.jsp" style="text-decoration:none; color:inherit;">
                    <div class="title-text">RECian's VoiceOut</div>
                </a>
                <div class="sub-tagline">Rajalakshmi Engineering College &bull; Student Portal</div>
            </div>
        </div>
        <div>
            <a href="../index.jsp" class="btn btn-secondary" style="padding: 0.5rem 1rem; font-size: 0.85rem;">&larr; Back to Home</a>
        </div>
    </header>

    <main class="container">
        <div class="auth-card">
            <h2>Student Sign In</h2>
            <p class="subtitle">Access your grievance dashboard using your college credentials</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <% if ("true".equals(request.getParameter("registered"))) { %>
                <div class="alert alert-success">
                    Registration successful! Please sign in with your college email.
                </div>
            <% } %>

            <form action="<%= request.getContextPath() %>/student/login" method="post">
                <div class="form-group">
                    <label for="email">College Email Address</label>
                    <input type="email" id="email" name="email" 
                           placeholder="username@rajalakshmi.edu.in" 
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                           required>
                    <small class="form-hint">Must end with @rajalakshmi.edu.in</small>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="Enter your password" required>
                </div>

                <button type="submit" class="btn btn-primary btn-block">Sign In &rarr;</button>
            </form>

            <div class="auth-footer">
                Don't have an account yet? 
                <a href="<%= request.getContextPath() %>/student/register" class="link-highlight">Register Here</a>
            </div>
        </div>
    </main>

    <footer>
        &copy; <%= java.time.Year.now() %> Rajalakshmi Engineering College &bull; RECian's VoiceOut
    </footer>
</body>
</html>
