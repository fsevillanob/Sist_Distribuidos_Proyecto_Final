import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
 
public class TareaServlet extends HttpServlet {
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect("login"); return;
        }
 
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        String username = (String) session.getAttribute("username");
 
        // Recoger parámetros de ordenación y búsqueda
        String orden       = req.getParameter("sort");
        String search      = req.getParameter("search");
        String searchValue = (search != null) ? search.replace("'", "&#39;") : "";
 
        try {
            int userId = DBManager.getUsuarioId(username);
 
            out.println("<!DOCTYPE html><html lang='es'><head>");
            out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<title>StudyApp – Mis Tareas</title>");
            out.println("<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css'>");
            out.println("<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css'>");
            out.println("<link rel='stylesheet' href='css/style.css'>");
 
            out.println("<style>");
            out.println(".dark-input { background-color: rgba(255,255,255,0.05) !important; color: #ffffff !important; border: 1px solid rgba(255,255,255,0.2) !important; }");
            out.println(".dark-input::placeholder { color: rgba(255,255,255,0.5) !important; }");
            out.println("::-webkit-calendar-picker-indicator { filter: invert(1); }");
            out.println("</style>");
            out.println("</head><body class='app-bg text-white'>");
 
            // NAVBAR
            out.println("<nav class='navbar navbar-expand-lg navbar-dark app-navbar mb-4'>");
            out.println("  <div class='container'>");
            out.println("    <a class='navbar-brand fw-bold' href='menu'><i class='bi bi-book-half me-2'></i>StudyApp</a>");
            out.println("    <div class='d-flex align-items-center'>");
            out.println("      <span class='text-white-50 me-3 small'><i class='bi bi-person-circle me-1'></i>" + username + "</span>");
            out.println("      <a href='menu' class='btn btn-outline-light btn-sm me-2'>Volver al Menú</a>");
            out.println("      <a href='logout' class='btn btn-danger btn-sm'><i class='bi bi-box-arrow-right'></i></a>");
            out.println("    </div>");
            out.println("  </div>");
            out.println("</nav>");
 
            out.println("<main class='container'>");
 
            // FILA SUPERIOR: BUSCADOR Y NUEVA TAREA
            out.println("<div class='row mb-4'>");
 
            // BUSCADOR (Izquierda)
            out.println("  <div class='col-md-4 mb-3 mb-md-0'>");
            out.println("    <div class='card menu-card p-3 h-100 border-0 text-white'>");
            out.println("      <form action='tareas' method='GET' class='d-flex flex-column h-100 justify-content-center'>");
            out.println("        <label class='small text-white-50 mb-2'><i class='bi bi-search me-1'></i>Buscar Tarea</label>");
            out.println("        <div class='input-group'>");
            out.println("          <input type='text' name='search' class='form-control dark-input' placeholder='Nombre de la tarea...' value='" + searchValue + "'>");
            if (search != null && !search.isEmpty()) {
                out.println("      <a href='tareas' class='btn btn-outline-danger' title='Limpiar búsqueda'><i class='bi bi-x-lg'></i></a>");
            }
            out.println("          <button type='submit' class='btn btn-outline-light'>Buscar</button>");
            out.println("        </div>");
            if (search != null && !search.isEmpty()) {
                out.println("    <div class='small text-warning mt-2'>Filtrando por: \"" + searchValue + "\"</div>");
            }
            out.println("      </form>");
            out.println("    </div>");
            out.println("  </div>");
 
            // FORMULARIO PARA CREAR TAREA (Derecha) → apunta a /tareas/crear
            out.println("  <div class='col-md-8'>");
            out.println("    <div class='card menu-card p-4 h-100 border-0 text-white'>");
            out.println("      <h5 class='mb-3 text-white'><i class='bi bi-plus-circle me-2'></i>Nueva Tarea</h5>");
            out.println("      <form action='tareas/crear' method='POST' class='row g-3'>");
            out.println("        <div class='col-md-4'><input type='text' name='nombre' class='form-control dark-input' placeholder='Nombre' required></div>");
            out.println("        <div class='col-md-4'><input type='text' name='desc' class='form-control dark-input' placeholder='Descripción'></div>");
            out.println("        <div class='col-md-4'><input type='datetime-local' name='fecha' class='form-control dark-input' required></div>");
            out.println("        <div class='col-12 text-end'><button type='submit' class='btn btn-primary fw-bold px-4'>Añadir a la lista</button></div>");
            out.println("      </form>");
            out.println("    </div>");
            out.println("  </div>");
 
            out.println("</div>"); // Fin del row superior
 
            // LISTA DE TAREAS
            out.println("<div class='card menu-card p-4 border-0 text-white'>");
            out.println("  <div class='d-flex justify-content-between align-items-center mb-4'>");
            out.println("    <h4 class='text-white'><i class='bi bi-list-check me-2 text-warning'></i>Mis Tareas</h4>");
            out.println("    <div class='btn-group'>");
            out.println("      <a href='tareas?sort=fecha" + (search != null ? "&search=" + search : "") + "' class='btn btn-sm btn-outline-light " + ("fecha".equals(orden) ? "active" : "") + "'>Por Fecha</a>");
            out.println("      <a href='tareas?sort=estado" + (search != null ? "&search=" + search : "") + "' class='btn btn-sm btn-outline-light " + ("estado".equals(orden) ? "active" : "") + "'>Por Estado</a>");
            out.println("    </div>");
            out.println("  </div>");
 
            out.println("<div class='table-responsive'>");
            out.println("<table class='table table-dark table-hover align-middle mb-0'>");
            out.println("<thead><tr><th>Tarea</th><th>Fecha</th><th>Estado</th><th class='text-end'>Acciones</th></tr></thead><tbody>");
 
            ResultSet rs = DBManager.listarTareasUsuario(userId, orden, search);
            boolean hayTareas = false;
 
            while (rs.next()) {
                hayTareas = true;
                String estado     = rs.getString("estado");
                String badgeClass = estado.equals("Terminada")  ? "bg-success" :
                                    estado.equals("En proceso") ? "bg-warning text-dark" : "bg-secondary";
 
                out.println("<tr>");
                out.println("  <td><div class='fw-bold'>" + rs.getString("nombre") + "</div>"
                          + "<div class='small text-white-50'>" + rs.getString("descripcion") + "</div></td>");
                out.println("  <td class='small'>" + rs.getTimestamp("fecha_hora").toString().substring(0, 16) + "</td>");
                out.println("  <td><span class='badge " + badgeClass + "'>" + estado + "</span></td>");
                out.println("  <td class='text-end'>");
 
                // Botón rotar estado → apunta a /tareas/actualizar
                String proximoEstado = estado.equals("Por hacer")  ? "En proceso" :
                                       estado.equals("En proceso") ? "Terminada"  : "Por hacer";
                out.println("    <form action='tareas/actualizar' method='POST' class='d-inline'>");
                out.println("      <input type='hidden' name='id' value='" + rs.getInt("id_tarea") + "'>");
                out.println("      <input type='hidden' name='nuevoEstado' value='" + proximoEstado + "'>");
                out.println("      <button type='submit' class='btn btn-sm btn-outline-info me-1' title='Pasar a " + proximoEstado + "'><i class='bi bi-arrow-repeat'></i></button>");
                out.println("    </form>");
 
                // Botón eliminar → apunta a /tareas/eliminar
                out.println("    <form action='tareas/eliminar' method='POST' class='d-inline'>");
                out.println("      <input type='hidden' name='id' value='" + rs.getInt("id_tarea") + "'>");
                out.println("      <button type='submit' class='btn btn-sm btn-outline-danger' onclick='return confirm(\"¿Borrar esta tarea?\")'><i class='bi bi-trash'></i></button>");
                out.println("    </form>");
 
                out.println("  </td></tr>");
            }
 
            if (!hayTareas) {
                out.println("<tr><td colspan='4' class='text-center text-white-50 py-4'>No se encontraron tareas.</td></tr>");
            }
 
            out.println("</tbody></table></div></div>");
            out.println("</main></body></html>");
 
        } catch (Exception e) {
            out.println("<div class='alert alert-danger m-4'>Error: " + e.getMessage() + "</div>");
        }
    }
}