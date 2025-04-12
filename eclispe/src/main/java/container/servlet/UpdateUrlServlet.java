package container.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import conn.Database;

@WebServlet("/api/updateurl")
public class UpdateUrlServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();

        try (PrintWriter out = response.getWriter()) {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Not authorized. Please login.");
                out.print(jsonResponse.toString());
                return;
            }

            String urlIdStr = request.getParameter("id");
            String targetUrl = request.getParameter("url");

            if (urlIdStr == null || urlIdStr.isEmpty() || targetUrl == null || targetUrl.isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Invalid parameters");
                out.print(jsonResponse.toString());
                return;
            }

            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "URL must start with http:// or https://");
                out.print(jsonResponse.toString());
                return;
            }

            int userId;
            int urlId;

            try {
                userId = ((Number) session.getAttribute("userId")).intValue();
                urlId = Integer.parseInt(urlIdStr);
            } catch (NumberFormatException e) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Invalid URL ID");
                out.print(jsonResponse.toString());
                return;
            }

            try (Database db = new Database()) {
               
                boolean urlExists = db.select(
                    "url_list",
                    "list_id",
                    null,
                    "list_id = ? AND user_id = ?",
                    new Object[]{urlId, userId},
                    null, null
                );

                List<Object> result = db.getResult();

                if (!urlExists || result.size() < 2 || ((List<?>) result.get(1)).isEmpty()) {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "URL not found!!");
                    out.print(jsonResponse.toString());
                    return;
                }

                boolean updated = db.update(
                    "url_list",
                    new String[]{"target_url"},
                    new Object[]{targetUrl},
                    "list_id = ? AND user_id = ?",
                    new Object[]{urlId, userId}
                );

                if (updated) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("message", "URL updated successfully");
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Failed to update URL");
                }
            }

            out.print(jsonResponse.toString());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success", false);
            errorResponse.put("message", "Server error: " + e.getMessage());

            try (PrintWriter out = response.getWriter()) {
                out.print(errorResponse.toString());
            }
        }
    }
}
