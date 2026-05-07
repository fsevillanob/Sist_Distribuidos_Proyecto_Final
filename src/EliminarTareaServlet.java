import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
 
public class EliminarTareaServlet extends HttpServlet {
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
 
        try {
            int idTarea = Integer.parseInt(req.getParameter("id"));
            int userId = DBManager.getUsuarioId((String) session.getAttribute("username"));
            DBManager.eliminarTarea(idTarea, userId);
            res.sendRedirect(req.getContextPath() + "/tareas");
        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/tareas?error=1");
        }
    }
}