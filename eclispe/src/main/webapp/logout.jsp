<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    HttpSession getSession = request.getSession(false);
    if (getSession != null) {
    	getSession.invalidate();
    }
    
    response.sendRedirect("login.jsp");
%>