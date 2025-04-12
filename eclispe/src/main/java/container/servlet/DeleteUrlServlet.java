package container.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.json.JSONObject;
import conn.Database;

@WebServlet("/api/deleteurl")
public class DeleteUrlServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Not authorized. Please login.");
                out.print(jsonResponse.toString());
                return;
            }

            String urlId = request.getParameter("id");
            int userId = ((Number) session.getAttribute("userId")).intValue();

            if (urlId == null || urlId.isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Invalid URL ID.");
                out.print(jsonResponse.toString());
                return;
            }

            try (Database db = new Database()) {
                boolean urlExists = db.select(
                    "url_list",
                    "*", 
                    null,
                    "list_id = ? AND user_id = ?",
                    new Object[]{Integer.parseInt(urlId), userId},
                    null,
                    1
                );

                List<Object> result = db.getResult();

                if (!urlExists || result.size() < 2 || ((List<?>) result.get(1)).isEmpty()) {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "URL not found!!");
                    out.print(jsonResponse.toString());
                    return;
                }

                boolean deleted = db.delete(
                    "url_list",
                    "list_id = ? AND user_id = ?",
                    new Object[]{Integer.parseInt(urlId), userId}
                );

                if (deleted) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("message", "URL deleted successfully.");
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Failed to delete URL.");
                }
            }

        } catch (NumberFormatException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid ID format.");
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Server error: " + e.getMessage());
            e.printStackTrace();
        }

        out.print(jsonResponse.toString());
    }
}
