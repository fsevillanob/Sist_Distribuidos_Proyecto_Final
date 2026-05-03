import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CalendarioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect("login"); return;
        }

        String action = req.getParameter("action");
        String username = (String) session.getAttribute("username");

        // --- MODO API: Devolver JSON para FullCalendar ---
        if ("events".equals(action)) {
            renderJson(res, username);
            return;
        }

        // --- MODO VISTA: Devolver el HTML del calendario ---
        renderHtml(res, username, req.getContextPath());
    }

    private void renderJson(HttpServletResponse res, String username) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {
            int userId = DBManager.getUsuarioId(username);
            // Reutilizamos el método de listar tareas (ordenado por fecha)
            ResultSet rs = DBManager.listarTareasUsuario(userId, "fecha", null);
            
            out.print("[");
            boolean first = true;
            while(rs.next()) {
                if(!first) out.print(",");
                String estado = rs.getString("estado");
                // Color según estado (Verde: Terminada, Naranja: En proceso, Gris: Pendiente)
                String color = estado.equals("Terminada") ? "#198754" : (estado.equals("En proceso") ? "#fd7e14" : "#6c757d");
                
                out.print("{");
                out.print("\"id\":\"" + rs.getInt("id_tarea") + "\",");
                out.print("\"title\":\"" + rs.getString("nombre").replace("\"", "\\\"") + "\",");
                out.print("\"start\":\"" + rs.getTimestamp("fecha_hora").toString().replace(" ", "T") + "\",");
                out.print("\"backgroundColor\":\"" + color + "\",");
                out.print("\"borderColor\":\"" + color + "\",");
                out.print("\"extendedProps\": { \"desc\": \"" + rs.getString("descripcion") + "\", \"status\": \"" + estado + "\" }");
                out.print("}");
                first = false;
            }
            out.print("]");
        } catch (Exception e) {
            res.setStatus(500);
        }
    }

    private void renderHtml(HttpServletResponse res, String username, String cp) throws IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<!DOCTYPE html><html lang='es'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<title>StudyApp – Calendario</title>");
        out.println("<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css'>");
        out.println("<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css'>");
        out.println("<link rel='stylesheet' href='css/style.css'>");
        // FullCalendar Core
        out.println("<script src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.11/index.global.min.js'></script>");
        
        out.println("<style>");
        out.println("  :root { --fc-border-color: rgba(255,255,255,0.1); }");
        out.println("  .fc { background: var(--card-bg); color: white; padding: 20px; border-radius: 16px; border: 1px solid var(--card-border); }");
        out.println("  .fc-theme-standard td, .fc-theme-standard th { border: 1px solid rgba(255,255,255,0.05); }");
        out.println("  .fc-button-primary { background-color: var(--accent) !important; border: none !important; }");
        out.println("  .fc-list-day-cushion { background: rgba(255,255,255,0.05) !important; }");
        out.println("  .fc-col-header-cell-cushion { color: #fff; text-decoration: none; padding: 8px 0; }");
        out.println("</style></head>");

        out.println("<body class='app-bg text-white'>");
        
        // NAVBAR COHESIVO
        out.println("<nav class='navbar navbar-expand-lg navbar-dark app-navbar mb-4'><div class='container'>");
        out.println("  <a class='navbar-brand fw-bold' href='menu'><i class='bi bi-book-half me-2'></i>StudyApp</a>");
        out.println("  <div class='d-flex align-items-center'>");
        out.println("    <a href='menu' class='btn btn-outline-light btn-sm me-2'>Volver</a>");
        out.println("    <a href='logout' class='btn btn-danger btn-sm'><i class='bi bi-box-arrow-right'></i></a>");
        out.println("  </div></div></nav>");

        out.println("<main class='container'>");
        out.println("  <div class='d-flex justify-content-between align-items-center mb-4'>");
        out.println("    <h3><i class='bi bi-calendar3 me-2 text-warning'></i>Calendario de Tareas</h3>");
        out.println("    <a href='tareas' class='btn btn-primary btn-sm'><i class='bi bi-list-task me-1'></i>Gestionar Tareas</a>");
        out.println("  </div>");
        
        out.println("  <div id='calendar' class='shadow-lg'></div>");
        out.println("</main>");

        // SCRIPT DEL CALENDARIO
        out.println("<script>");
        out.println("document.addEventListener('DOMContentLoaded', function() {");
        out.println("  var calendarEl = document.getElementById('calendar');");
        out.println("  var calendar = new FullCalendar.Calendar(calendarEl, {");
        out.println("    initialView: 'dayGridMonth',");
        out.println("    locale: 'es',");
        out.println("    headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek,listWeek' },");
        out.println("    buttonText: { today:'Hoy', month:'Mes', week:'Semana', list:'Agenda' },");
        out.println("    events: 'calendario?action=events',"); // Llama al propio servlet en modo JSON
        out.println("    eventClick: function(info) {");
        out.println("      alert('Tarea: ' + info.event.title + '\\nEstado: ' + info.event.extendedProps.status + '\\nNota: ' + info.event.extendedProps.desc);");
        out.println("    }");
        out.println("  });");
        out.println("  calendar.render();");
        out.println("});");
        out.println("</script>");

        out.println("</body></html>");
    }
}