import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Sirve el menú principal. Requiere sesión activa.
 */
public class MenuServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String rol = (String) session.getAttribute("rol");
        boolean esAdmin = "admin".equals(rol);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='es'>");
        out.println("<head>");
        out.println("  <meta charset='UTF-8'>");
        out.println("  <meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("  <title>StudyApp – Menú</title>");
        out.println("  <link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css'>");
        out.println("  <link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css'>");
        out.println("  <link rel='stylesheet' href='" + req.getContextPath() + "/css/style.css'>");
        out.println("</head>");
        out.println("<body class='app-bg d-flex flex-column min-vh-100'>");

        // Navbar
        out.println("<nav class='navbar navbar-dark app-navbar px-4'>");
        out.println("  <span class='navbar-brand fw-bold'><i class='bi bi-book-half me-2'></i>StudyApp</span>");
        out.println("  <div class='d-flex align-items-center gap-3'>");
        out.println("    <span class='text-white-50 small'><i class='bi bi-person-circle me-1'></i>" + escHtml(username) + " <span class='badge " + (esAdmin ? "bg-warning text-dark" : "bg-secondary") + " ms-1'>" + rol + "</span></span>");
        out.println("    <a href='" + req.getContextPath() + "/logout' class='btn btn-outline-light btn-sm'><i class='bi bi-box-arrow-right me-1'></i>Salir</a>");
        out.println("  </div>");
        out.println("</nav>");

        // Contenido principal
        out.println("<main class='container py-5 flex-grow-1'>");
        out.println("  <div class='text-center mb-5'>");
        out.println("    <h1 class='display-5 fw-bold text-white'>¡Hola, " + escHtml(username) + "!</h1>");
        out.println("    <p class='text-white-50 fs-5'>¿Qué quieres hacer hoy?</p>");
        out.println("  </div>");

        out.println("  <div class='row g-4 justify-content-center'>");

        // Tarjeta Pomodoro
        out.println("  <div class='col-md-4 col-sm-6'>");
        out.println("    <a href='" + req.getContextPath() + "/pomodoro.html' class='text-decoration-none'>");
        out.println("      <div class='card menu-card h-100 text-center p-4'>");
        out.println("        <div class='menu-icon mb-3'><i class='bi bi-stopwatch'></i></div>");
        out.println("        <h4 class='card-title text-white'>Técnica Pomodoro</h4>");
        out.println("        <p class='card-text text-white-50'>Sesiones de estudio cronometradas con pausas. ¡Mejora tu concentración!</p>");
        out.println("      </div>");
        out.println("    </a>");
        out.println("  </div>");

        // Tarjeta Admin (solo si es admin)
        if (esAdmin) {
            out.println("  <div class='col-md-4 col-sm-6'>");
            out.println("    <a href='" + req.getContextPath() + "/admin' class='text-decoration-none'>");
            out.println("      <div class='card menu-card menu-card--admin h-100 text-center p-4'>");
            out.println("        <div class='menu-icon mb-3'><i class='bi bi-shield-lock'></i></div>");
            out.println("        <h4 class='card-title text-white'>Panel de Admin</h4>");
            out.println("        <p class='card-text text-white-50'>Gestiona usuarios, crea cuentas y cambia contraseñas.</p>");
            out.println("      </div>");
            out.println("    </a>");
            out.println("  </div>");
        }

        // Tarjeta Tareas
        out.println("  <div class='col-md-4 col-sm-6'>");
        out.println("    <a href='tareas' class='text-decoration-none'>"); //rutas relativas, funciona, pero PENSAR en cambiar 
        out.println("      <div class='card menu-card h-100 text-center p-4'>");
        out.println("        <div class='menu-icon mb-3'><i class='bi bi-calendar3'></i></div>");
        out.println("        <h4 class='card-title text-white'>Gestión de Tareas</h4>");
        out.println("        <p class='card-text text-white-50'>Organiza tus tareas.</p>");
        out.println("      </div>");
        out.println("    </a>");
        out.println("  </div>");

        out.println("  </div>"); // row

        out.println("  <div class='col-md-4 col-sm-6'>");
        out.println("    <a href='calendario' class='text-decoration-none'>");
        out.println("      <div class='card menu-card h-100 text-center p-4'>");
        out.println("        <div class='menu-icon mb-3'><i class='bi bi-calendar-event text-info'></i></div>");
        out.println("        <h4 class='card-title text-white'>Calendario</h4>");
        out.println("        <p class='card-text text-white-50'>Visualiza tus entregas y exámenes en vista mensual o semanal.</p>");
        out.println("      </div>");
        out.println("    </a>");
        out.println("  </div>");




        out.println("</main>");

        out.println("<footer class='text-center text-white-50 py-3 small'>StudyApp &copy; 2025</footer>");
        out.println("<script src='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js'></script>");
        out.println("</body></html>");
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
