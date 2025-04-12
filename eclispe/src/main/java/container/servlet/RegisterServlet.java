package container.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import conn.Database;
import services.Utils;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Database db = new Database()) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            Map<String, String> errors = new HashMap<>();

            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");

            if (name == null || name.trim().isEmpty()) errors.put("name", "Name is required");
            if (email == null || email.trim().isEmpty()) {
                errors.put("email", "Email is required");
            } else if (!new Utils().isValidEmail(email)) {
                errors.put("email", "Invalid email format");
            }
            if (password == null || password.trim().isEmpty()) {
                errors.put("password", "Password is required");
            } else if (password.length() < 8) {
                errors.put("password", "Password must be 8+ characters");
            }
            if (!password.equals(confirmPassword)) {
                errors.put("confirmPassword", "Passwords don't match");
            }

            if (email != null && errors.get("email") == null) {
                db.select("users", "email", null, "email = ?", new Object[] {email}, null, null);
                List<Object> result = db.getResult();
                
                if (result.size() >= 2) {
                    List<Object[]> rows = (List<Object[]>) result.get(1);
                    if (!rows.isEmpty()) {
                        errors.put("email", "Email already registered");
                    }
                }
            }

            if (errors.isEmpty()) {
                
            	String pwd = new Utils().hashPassword(password);
            	
                String[] columns = {"NAME", "EMAIL", "PWD"};
                Object[] values = {name, email, pwd};
                
                if (db.insert("users", columns, values)) {
                    out.print("{\"success\": true}");
                } else {
                    errors.put("server", "Registration failed. Please try again.");
                    out.print(new Utils().buildErrorJson(errors));
                }
            } else {
                out.print(new Utils().buildErrorJson(errors));
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    
}