<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    HttpSession existingSession = request.getSession(false);
    if (existingSession != null && existingSession.getAttribute("userId") != null) {
        response.sendRedirect("index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta
      name="viewport"
      content="width=device-width, initial-scale=1.0"
    />
    <title>Login to ChatLook</title>

    <!-- Bootstrap CSS -->
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <!-- custom css -->
    <link
      href="assets/css/style.css"
      rel="stylesheet"
      type="text/css"
    />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js"></script>
  </head>
  <body class="d-flex align-items-center justify-content-center" style="height: 100vh">
  <main>
    <div class="container">
      <div class="row">
        <img
            src="./assets/images/logo.png"
            alt="Logo"
            class="logo_wrapper m-auto"
          />
          <h1 class="text-center mt-4 text-color-own">
            Sign in to LookShortUrl
          </h1>
        <form class="d-flex form__wrapper flex-column gap-3 mt-4" onsubmit="return validateLogin(event)">
          <div class="form-group">
            <input
              type="email"
              class="form-control"
              id="email"
              placeholder="example@example.com"
            />
            <small class="text-danger" id="emailError"></small>
          </div>
          <div class="form-group">
            <input
              type="password"
              class="form-control"
              id="password"
              placeholder="Password"
            />
            <small class="text-danger" id="passwordError"></small>
            <div id="loginSuccess" class="alert alert-success mt-2 d-none"></div>
          </div>
          <div class="form-group">
            <button type="submit" class="btn w-100 bg-own-color text-white btn-block">
              Sign in
            </button>
          </div>
          <div class="mt-3 d-flex flex-column flex-lg-row gap-2 gap-lg-4 align-items-center justify-content-center">
              <a href="register.jsp"
                class="text-color-own text-decoration-none">New to ChatShortUrl?</a>
            </div>
        </form>
      </div>
    </div>
  </main>

  <script>
    function validateLogin(event) {
      event.preventDefault();
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value.trim();
      const emailError = document.getElementById("emailError");
      const passwordError = document.getElementById("passwordError");
      const successMessage = document.getElementById("loginSuccess");

      // Clear previous messages
      emailError.textContent = "";
      passwordError.textContent = "";
      successMessage.classList.add("d-none");

      let isValid = true;

      if (!email) {
        emailError.textContent = "Email is required.";
        isValid = false;
      }
      if (!password) {
        passwordError.textContent = "Password is required.";
        isValid = false;
      }

      if (isValid) {
        const formData = new URLSearchParams();
        formData.append("email", email);
        formData.append("password", password);

        fetch("/ShortUrlProject/api/auth/login", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: formData,
        })
          .then((response) => response.json())
          .then((data) => {
            if (data.success) {
              successMessage.classList.remove("d-none");
              successMessage.textContent = "Login successful! Redirecting...";
              
              setTimeout(() => {
                window.location.href = "index.jsp"; 
              }, 2000);
            } else {
              if (data.error) {
                emailError.textContent = data.error.email || "";
                passwordError.textContent = data.error.password || "";
              }
            }
          })
          .catch((error) => {
            console.error("Error:", error);
            passwordError.textContent = "An error occurred. Please try again.";
          });
      }
    }
  </script>
</body>
</html>
