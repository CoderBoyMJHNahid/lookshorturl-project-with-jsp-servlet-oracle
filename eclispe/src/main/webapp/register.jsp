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
    <title>Sign Up to LookShortUrl</title>

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
  <body
    class="d-flex align-items-center justify-content-center"
    style="height: 100vh"
  >
    <main>
      <div class="container">
        <div class="row">
          <img
            src="./assets/images/logo.png"
            alt="Logo"
            class="logo_wrapper m-auto"
          />
          <h1 class="text-center mt-4">Register to LookShortUrl</h1>
          <form
            class="d-flex form__wrapper flex-column gap-3 mt-4"
            onsubmit="return validateForm(event)"
          >
            <div class="form-group">
              <input
                type="text"
                class="form-control"
                id="name"
                placeholder="Enter your name"
                name="fname"
              />
              <small
                class="text-danger"
                id="nameError"
              ></small>
            </div>
            <div class="form-group">
              <input
                type="email"
                class="form-control"
                id="email"
                placeholder="Enter your email address"
              />
              <small
                class="text-danger"
                id="emailError"
              ></small>
            </div>
            <div class="form-group">
              <input
                type="password"
                class="form-control"
                id="pwd"
                placeholder="Enter your password"
                name="pwd"
              />
              <small
                class="text-danger"
                id="pwdError"
              ></small>
            </div>
            <div class="form-group">
              <input
                type="password"
                class="form-control"
                id="confirm_pwd"
                placeholder="Re-type your password"
              />
              <small
                class="text-danger"
                id="confirmPwdError"
              ></small>
              <div id="successMessage" class="alert alert-success mt-2 d-none"></div>
            </div>
            <div class="form-group">
              <button
                type="submit"
                class="btn bg-own-color w-100 btn-block"
              >
                Sign up
              </button>
            </div>
            <div
              class="mt-3 d-flex flex-column flex-lg-row gap-2 gap-lg-4 align-items-center justify-content-center"
            >
              <a
                href="login.jsp"
                class="text-color-own text-decoration-none"
                >Have a account?</a
              >
            </div>
          </form>
        </div>
      </div>
    </main>

    <script>
  function validateForm(event) {
    event.preventDefault();
    let isValid = true;

    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("pwd").value.trim();
    const confirmPassword = document.getElementById("confirm_pwd").value.trim();

    document.getElementById("nameError").textContent = "";
    document.getElementById("emailError").textContent = "";
    document.getElementById("pwdError").textContent = "";
    document.getElementById("confirmPwdError").textContent = "";

    if (!name) {
      document.getElementById("nameError").textContent = "Name is required.";
      isValid = false;
    }
    if (!email) {
      document.getElementById("emailError").textContent = "Email is required.";
      isValid = false;
    }
    if (!password) {
      document.getElementById("pwdError").textContent = "Password is required.";
      isValid = false;
    }
    if (!confirmPassword) {
      document.getElementById("confirmPwdError").textContent = "Please confirm your password.";
      isValid = false;
    }
    if (password && confirmPassword && password !== confirmPassword) {
      document.getElementById("confirmPwdError").textContent = "Passwords do not match.";
      isValid = false;
    }

    if (isValid) {
      const formData = new URLSearchParams();
      formData.append("name", name);
      formData.append("email", email);
      formData.append("password", password);
      formData.append("confirmPassword", confirmPassword);

      fetch("/ShortUrlProject/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: formData,
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
        	  successMessage.innerHTML = 
                'Registration successful! Redirecting to login page...';
        	  successMessage.classList.remove('d-none');
        	  
              setTimeout(() => {
                window.location.href = "login.jsp";
              }, 2000);
          } else {
        	  
        	  successMessage.classList.add('d-none');
        	  
            if (data.errors) {
              console.log(data.errors);
              document.getElementById("nameError").textContent = data.errors.name || "";
              document.getElementById("emailError").textContent = data.errors.email || "";
              document.getElementById("pwdError").textContent = data.errors.password || "";
              document.getElementById("confirmPwdError").textContent = data.errors.confirmPassword || "";
            }
          }
        })
        .catch((error) => {
          console.error("Error:", error);
          successMessage.classList.add('d-none');
        });
    }
  }
</script>
  </body>
</html>
