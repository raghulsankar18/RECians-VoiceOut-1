<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Registration - RECian's VoiceOut</title>
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
                <div class="sub-tagline">Rajalakshmi Engineering College &bull; Student Registration</div>
            </div>
        </div>
        <div>
            <a href="<%= request.getContextPath() %>/student/login" class="btn btn-secondary" style="padding: 0.5rem 1rem; font-size: 0.85rem;">Sign In</a>
        </div>
    </header>

    <main class="container">
        <div class="auth-card">
            <h2>Create Student Account</h2>
            <p class="subtitle">Join the RECian community to voice concerns and improve campus life</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <form action="<%= request.getContextPath() %>/student/register" method="post">
                <div class="form-group">
                    <label for="fullName">Full Name</label>
                    <input type="text" id="fullName" name="fullName" 
                           placeholder="e.g. Navaneethan S" 
                           value="<%= request.getAttribute("fullName") != null ? request.getAttribute("fullName") : "" %>"
                           required>
                </div>

                <div class="form-group">
                    <label for="email">REC College Email</label>
                    <input type="email" id="email" name="email" 
                           placeholder="username@rajalakshmi.edu.in" 
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                           pattern=".+@rajalakshmi\.edu\.in$"
                           title="Must end with @rajalakshmi.edu.in"
                           required>
                    <small class="form-hint">Only official REC email addresses (@rajalakshmi.edu.in) are permitted.</small>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" 
                           placeholder="Create a strong password (min 6 characters)" 
                           minlength="6" required>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" 
                           placeholder="Re-enter your password" 
                           minlength="6" required>
                </div>

                <button type="submit" class="btn btn-primary btn-block">Register &rarr;</button>
            </form>

            <div class="auth-footer">
                Already registered? 
                <a href="<%= request.getContextPath() %>/student/login" class="link-highlight">Sign In Here</a>
            </div>
        </div>
    </main>

    <footer>
        &copy; <%= java.time.Year.now() %> Rajalakshmi Engineering College &bull; RECian's VoiceOut
    </footer>
</body>
</html>
