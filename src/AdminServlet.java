import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

/**
 * Panel de administración: listar, crear, cambiar password y eliminar usuarios.
 * Solo accesible para rol "admin".
 */
public class AdminServlet extends HttpServlet {

    // ─── GET: mostrar panel ──────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!checkAdmin(req, res)) return;

        String msg   = req.getParameter("msg");
        String error = req.getParameter("error");

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='es'>");
        out.println("<head>");
        out.println("  <meta charset='UTF-8'>");
        out.println("  <meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("  <title>StudyApp – Admin</title>");
        out.println("  <link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css'>");
        out.println("  <link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css'>");
        out.println("  <link rel='stylesheet' href='" + req.getContextPath() + "/css/style.css'>");
        out.println("</head>");
        out.println("<body class='app-bg d-flex flex-column min-vh-100'>");

        // Navbar
        String username = (String) req.getSession().getAttribute("username");
        out.println("<nav class='navbar navbar-dark app-navbar px-4'>");
        out.println("  <span class='navbar-brand fw-bold'><i class='bi bi-book-half me-2'></i>StudyApp</span>");
        out.println("  <div class='d-flex align-items-center gap-3'>");
        out.println("    <span class='text-white-50 small'><i class='bi bi-person-circle me-1'></i>" + escHtml(username) + " <span class='badge bg-warning text-dark ms-1'>admin</span></span>");
        out.println("    <a href='" + req.getContextPath() + "/menu' class='btn btn-outline-light btn-sm'><i class='bi bi-house me-1'></i>Menú</a>");
        out.println("    <a href='" + req.getContextPath() + "/logout' class='btn btn-outline-danger btn-sm'><i class='bi bi-box-arrow-right me-1'></i>Salir</a>");
        out.println("  </div>");
        out.println("</nav>");

        out.println("<main class='container py-4 flex-grow-1'>");
        out.println("  <h2 class='text-white fw-bold mb-4'><i class='bi bi-shield-lock me-2'></i>Panel de Administración</h2>");

        // Alertas
        if (msg != null) {
            out.println("  <div class='alert alert-success alert-dismissible fade show' role='alert'>" + escHtml(msg) + "<button type='button' class='btn-close' data-bs-dismiss='alert'></button></div>");
        }
        if (error != null) {
            out.println("  <div class='alert alert-danger alert-dismissible fade show' role='alert'>" + escHtml(error) + "<button type='button' class='btn-close' data-bs-dismiss='alert'></button></div>");
        }

        // ── Formulario crear usuario ──────────────────────────────────────────
        out.println("  <div class='card admin-card mb-4 p-4'>");
        out.println("    <h5 class='text-white mb-3'><i class='bi bi-person-plus me-2'></i>Crear nuevo usuario</h5>");
        out.println("    <form method='post' action='" + req.getContextPath() + "/admin/crear' class='row g-3'>");
        out.println("      <div class='col-md-4'>");
        out.println("        <input type='text' class='form-control' name='username' placeholder='Nombre de usuario' required>");
        out.println("      </div>");
        out.println("      <div class='col-md-3'>");
        out.println("        <input type='text' class='form-control' name='password' placeholder='Contraseña' required>");
        out.println("      </div>");
        out.println("      <div class='col-md-3'>");
        out.println("        <select class='form-select' name='rol'>");
        out.println("          <option value='user'>user</option>");
        out.println("          <option value='admin'>admin</option>");
        out.println("        </select>");
        out.println("      </div>");
        out.println("      <div class='col-md-2'>");
        out.println("        <button type='submit' class='btn btn-success w-100'><i class='bi bi-plus-circle me-1'></i>Crear</button>");
        out.println("      </div>");
        out.println("    </form>");
        out.println("  </div>");

        // ── Tabla de usuarios ─────────────────────────────────────────────────
        out.println("  <div class='card admin-card p-4'>");
        out.println("    <h5 class='text-white mb-3'><i class='bi bi-people me-2'></i>Usuarios registrados</h5>");
        out.println("    <div class='table-responsive'>");
        out.println("    <table class='table table-dark table-hover align-middle'>");
        out.println("      <thead><tr><th>ID</th><th>Usuario</th><th>Rol</th><th>Nueva contraseña</th><th>Acciones</th></tr></thead>");
        out.println("      <tbody>");

        try (Connection con = DBManager.getConnection();
             ResultSet rs = DBManager.getUsuarios(con)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String uname = rs.getString("username");
                String rol = rs.getString("rol");

                out.println("<tr>");
                out.println("  <td>" + id + "</td>");
                out.println("  <td>" + escHtml(uname) + "</td>");
                out.println("  <td><span class='badge " + ("admin".equals(rol) ? "bg-warning text-dark" : "bg-secondary") + "'>" + rol + "</span></td>");

               // Cambiar contraseña
                out.println("  <td>");
                out.println("    <form method='post' action='" + req.getContextPath() + "/admin/password' class='d-flex gap-2'>");
                out.println("      <input type='hidden' name='id' value='" + id + "'>");
                out.println("      <input type='text' class='form-control form-control-sm' name='newpassword' placeholder='Nueva contraseña' required style='max-width:200px'>");
                out.println("      <button type='submit' class='btn btn-warning btn-sm'><i class='bi bi-key'></i></button>");
                out.println("    </form>");
                out.println("  </td>");

                // Eliminar
                out.println("  <td>");
                out.println("    <form method='post' action='" + req.getContextPath() + "/admin/eliminar' onsubmit='return confirm(\"¿Seguro que quieres eliminar a " + escHtml(uname) + "?\")'>");
                out.println("      <input type='hidden' name='id' value='" + id + "'>");
                out.println("      <button type='submit' class='btn btn-danger btn-sm'><i class='bi bi-trash'></i></button>");
                out.println("    </form>");
                out.println("  </td>");

                out.println("</tr>");
            }

        } catch (SQLException e) {
            out.println("<tr><td colspan='5' class='text-danger'>Error al cargar usuarios: " + e.getMessage() + "</td></tr>");
        }

        out.println("      </tbody>");
        out.println("    </table>");
        out.println("    </div>");
        out.println("  </div>");

        out.println("</main>");
        out.println("<footer class='text-center text-white-50 py-3 small'>StudyApp &copy; 2025</footer>");
        out.println("<script src='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js'></script>");
        out.println("</body></html>");
    }

    private boolean checkAdmin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!"admin".equals(session.getAttribute("rol"))) {
            res.sendRedirect(req.getContextPath() + "/menu");
            return false;
        }
        return true;
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
