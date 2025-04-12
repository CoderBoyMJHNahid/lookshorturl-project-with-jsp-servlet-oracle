package container.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import conn.Database;
import services.Utils;

@WebServlet("/api/addurl")
public class AddUrlServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "User not authenticated");
                out.print(jsonResponse.toString());
                return;
            }
            
            int userId = ((Number) session.getAttribute("userId")).intValue();
            
            String targetUrl = request.getParameter("url");
            if (targetUrl == null || targetUrl.trim().isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "URL cannot be empty");
                out.print(jsonResponse.toString());
                System.out.print("Here again");
                return;
            }
            
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "URL must start with http:// or https://");
                out.print(jsonResponse.toString());
                
                return;
            }
            
            String shortCode = new Utils().generateShortCode();
            
            try (Database db = new Database()) {
                Object[] params = {shortCode, targetUrl, 0, userId};
                boolean inserted = db.insert("url_list", 
                        new String[]{"short_url", "target_url", "t_view", "user_id"}, 
                        params);
                
                
                if (inserted) {
                    db.select("url_list", "list_id", null, "short_url = ?", new Object[]{shortCode}, null, null);
                    List<Object> result = db.getResult();
                    int urlId = 0;
                    if (result.size() >= 2) {
                    	 List<Object[]> rows = (List<Object[]>) result.get(1);
                        if (!rows.isEmpty()) {
                            Object[] row = rows.get(0);
                            urlId = ((Number)row[0]).intValue();
                        }
                    }
                    
                    jsonResponse.put("success", true);
                    jsonResponse.put("message", "URL shortened successfully");
                    jsonResponse.put("id", urlId);
                    jsonResponse.put("shortCode", shortCode);
                    jsonResponse.put("shortUrl", db.hostname + shortCode);
                    jsonResponse.put("targetUrl", targetUrl);
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Error saving URL");
                }
            }
        } catch (Exception e) {
        	System.out.println(e);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Server error: " + e.getMessage());
        }
        
        out.print(jsonResponse.toString());
        out.flush();
    }
    
    
}