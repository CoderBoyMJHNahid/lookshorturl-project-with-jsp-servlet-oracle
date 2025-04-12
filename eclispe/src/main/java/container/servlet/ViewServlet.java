package container.servlet;

import conn.Database;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/v/*") 
public class ViewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ViewServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); 
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Short code is missing");
            return;
        }

        String shortCode = pathInfo.substring(1);

        try (Database db = new Database()) {
            
        	db.select(
                "url_list",
                "*",
                null,
                "short_url = ?",
                new Object[]{shortCode},
                null,
                1
            );

            List<Object> result = db.getResult();
            List<Object[]> rows = (List<Object[]>) result.get(1);
            if (rows.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Short link not found");
                return;
            }

            Object[] row = rows.get(0);
            String targetUrl = row[2].toString(); 
            
            db.update(
                "url_list",
                new String[]{"t_view"},
                new Object[]{((BigDecimal) row[3]).intValue() + 1},
                "short_url = ?",
                new Object[]{shortCode}
            );

            response.sendRedirect(targetUrl);

        } catch (Exception e) {
        	System.out.print(e);
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong: " + e.getMessage());
        }
    }
}
