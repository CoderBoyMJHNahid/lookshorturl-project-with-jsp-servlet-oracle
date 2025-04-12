<%@page import="java.util.List"%>
<%@page import="conn.Database"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    HttpSession existingSession = request.getSession(false);
    if (existingSession == null || existingSession.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Home page - LookShortUrl</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="assets/css/style.css" rel="stylesheet" type="text/css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js"></script>

</head>
<body style="background: #f9f9f9">
    <main>
      <header>
        <nav class="navbar navbar-expand-lg">
          <div class="container">
            <a
              class="navbar-brand"
              href="index.jsp"
              ><img
                class="logo-img"
                src="assets/images/logo.png"
                alt="Logo"
            /></a>
            <button
              class="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarSupportedContent"
              aria-controls="navbarSupportedContent"
              aria-expanded="false"
              aria-label="Toggle navigation"
            >
              <span class="navbar-toggler-icon"></span>
            </button>
            <div
              class="collapse navbar-collapse justify-content-end"
              id="navbarSupportedContent"
            >
              <ul class="navbar-nav mb-2 mb-lg-0">
                <li class="nav-item">
                  <a
                    class="nav-link"
                    aria-current="page"
                    href="logout.jsp"
                    >Logout</a
                  >
                </li>
              </ul>
            </div>
          </div>
        </nav>
      </header>
        <section class="container">
            <div class="row">
                <div class="col-12">
                    <h1 class="text-center my-4 text-color-own">Welcome to LookShortUrl</h1>
                </div>
                <div class="col-12">
                    <div class="content_wrapper">
                        <h2 class="fw-bold text-center">Paste the URL to be shortened</h2>
                        <form id="urlForm" class="d-flex gap-3 mt-3">
                            <input type="url" name="url" id="urlInput" 
                                   class="form-control" placeholder="https://example.com" 
                                   required pattern="https?://.+">
                            <button type="submit" class="btn bg-own-color px-3 py-2" id="submitBtn">
                                Shorten
                            </button>
                        </form>
                        <div class="url_table_wrapper mt-3">
                            <p class="fs-5 fw-bold text-color-own">Your short URL list:</p>
                            <div id="errorContainer"></div>
                            <table class="table table-striped table-hover">
                                <thead>
                                    <tr>
                                        <th scope="col">#</th>
                                        <th scope="col">Short URL</th>
                                        <th scope="col">Target URL</th>
                                        <th scope="col">Visits</th>
                                        <th scope="col">Actions</th>
                                    </tr>
                                </thead>
                                <tbody id="urlTableBody">
                                    <%
                                        try (Database db = new Database()) {
                                            Number userId = (Number) session.getAttribute("userId");
                                            db.select(
                                                    "url_list",
                                                    "*",
                                                    null,
                                                    "user_id = ?", 
                                                    new Object[]{userId.intValue()},
                                                    null, null
                                                    );
                                            List<Object> result = db.getResult();
                                            if (result.size() >= 2 && ((List<Object[]>) result.get(1)).size() > 0) {
                                                List<Object[]> rows = (List<Object[]>) result.get(1);
                                                int i = 1;
                                                for (Object[] row : rows) {
                                                    String fullUrl = db.hostname + row[1];
                                                    String targetUrl = row[2].toString();
                                                    String truncatedTargetUrl = targetUrl.length() > 6 ? targetUrl.substring(0, 6) + "..." : targetUrl;
                                    %>
                                                <tr>
                                                    <td scope="row"><%= i %></td>
                                                    <td>
                                                        <a href="<%= fullUrl %>" class="short-url" target="_blank">
                                                            <%= fullUrl %>
                                                        </a>
                                                        <i class="fa-regular fa-copy copy-icon" 
                                                           onclick="copyToClipboard('<%= fullUrl %>', this)" 
                                                           title="Copy to clipboard"></i>
                                                    </td>
                                                    <td title="<%= targetUrl %>">
                                                        <a href="<%= targetUrl %>" class="target-url" target="_blank">
                                                            <%= truncatedTargetUrl %>
                                                        </a>
                                                    </td>
                                                    <td><%= row[3] %></td>
                                                    <td>
                                                        <button
                                                            class="btn btn-sm btn-success edit-btn"
                                                            data-bs-toggle="modal"
                                                            data-bs-target="#editModal"
                                                            data-url-id="<%= row[0] %>"
                                                            data-url="<%= targetUrl %>"
                                                        >
                                                            <i class="fa-solid fa-pen-to-square"></i>
                                                        </button>
                                                        <button class="btn btn-sm btn-danger delete-btn" data-url-id="<%= row[0] %>">
                                                            <i class="fa-solid fa-trash"></i>
                                                        </button>
                                                    </td>
                                                </tr>
                                   		<%
                                                i++;
                                                }
                                            } else {
                                   		%>
                                                <tr class="empty-state-row">
                                                    <td colspan="5" class="text-center text-muted py-4">
                                                        No URLs found. Create your first one!
                                                    </td>
                                                </tr>
                                    <%
                                            }
                                        } catch (Exception e) {
                                            out.println("Error: " + e.getMessage());
                                        }
                                    %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Edit Modal -->
        <div class="modal fade" id="editModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Edit URL</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <form id="editForm">
                            <input type="hidden" id="editUrlId">
                            <div class="mb-3">
                                <label class="form-label">Destination URL</label>
                                <input type="url" id="editUrlInput" 
                                       class="form-control" required 
                                       pattern="https?://.+">
                            </div>
                            <button type="submit" class="btn bg-own-color w-100">
                                Update URL
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <!-- Delete Confirmation Modal -->
        <div class="modal fade" id="deleteModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Confirm Delete</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <p>Are you sure you want to delete this short URL? This action cannot be undone.</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-danger" id="confirmDeleteBtn">Delete</button>
                    </div>
                </div>
            </div>
        </div>
        
    </main>
	<!-- Toast Container -->
	<div id="toast-container"></div>
    <script src="assets/js/app.js"></script>
</body>
</html>