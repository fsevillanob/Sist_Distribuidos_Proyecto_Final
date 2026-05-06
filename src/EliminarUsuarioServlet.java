import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class EliminarUsuarioServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!checkAdmin(req, res)) return;

        int id = Integer.parseInt(req.getParameter("id"));

        try {
            DBManager.eliminarUsuario(id);
            res.sendRedirect(req.getContextPath() + "/admin?msg=Usuario+eliminado");
        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/admin?error=" +
                java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
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
}