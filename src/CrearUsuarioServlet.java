import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class CrearUsuarioServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!checkAdmin(req, res)) return;
        req.setCharacterEncoding("UTF-8");

        String u = req.getParameter("username");
        String p = req.getParameter("password");
        String r = req.getParameter("rol");

        try {
            DBManager.crearUsuario(u, p, r);
            res.sendRedirect(req.getContextPath() + "/admin?msg=Usuario+creado+correctamente");
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