package container.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import conn.Database;
import services.Utils;

@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Map<String, String> errors = new HashMap<>();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Database db = new Database()) {
            if (email == null || email.trim().isEmpty()) {
                errors.put("email", "Email is required");
            }
            if (password == null || password.trim().isEmpty()) {
                errors.put("password", "Password is required");
            }

            if (errors.isEmpty()) {
            	
                db.select("users", "user_id, email, pwd", null, "email = ?", new Object[]{email}, null, null);
                List<Object> result = db.getResult();

                if (result.size() >= 2) {
                    List<Object[]> rows = (List<Object[]>) result.get(1);
                    if (rows.isEmpty()) {
                        errors.put("email", "Email not registered");
                    } else {
                        Object[] user = rows.get(0);
                        String storedpwd = (String) user[2];
                        String pwd = new Utils().hashPassword(password);                     
                        if (!pwd.equals(storedpwd)) {
                            errors.put("password", "Invalid password");
                        } else {
                            HttpSession session = request.getSession();
                            BigDecimal userIdBigDec = (BigDecimal) user[0]; 
                            int userId = userIdBigDec.intValue();
                            session.setAttribute("userId", userId);
                            session.setAttribute("userEmail", user[1]);
                            session.setMaxInactiveInterval(30 * 60);

                            out.print("{\"success\": true}");
                            return;
                        }
                    }
                } else {
                    errors.put("email", "Email not registered");
                }
            }

            out.print(buildErrorJson(errors));

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    private String buildErrorJson(Map<String, String> errors) {
        StringBuilder json = new StringBuilder("{\"success\": false, \"error\": {");
        int count = 0;
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            if (count++ > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\": \"")
                .append(entry.getValue()).append("\"");
        }
        json.append("}}");
        return json.toString();
    }
}