import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
 
public class CrearTareaServlet extends HttpServlet {
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
 
        try {
            int userId = DBManager.getUsuarioId((String) session.getAttribute("username"));
            DBManager.crearTarea(
                userId,
                req.getParameter("nombre"),
                req.getParameter("desc"),
                req.getParameter("fecha"),
                "Por hacer"
            );
            res.sendRedirect(req.getContextPath() + "/tareas");
        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/tareas?error=1");
        }
    }
}