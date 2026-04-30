import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Maneja GET (mostrar formulario) y POST (procesar credenciales).
 */
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // Si ya está logueado, va al menú directamente
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            res.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        req.getRequestDispatcher("/login.html").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            String rol = DBManager.validarLogin(username, password);
            if (rol != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("username", username);
                session.setAttribute("rol", rol);
                res.sendRedirect(req.getContextPath() + "/menu");
            } else {
                res.sendRedirect(req.getContextPath() + "/login?error=1");
            }
        } catch (Exception e) {
            throw new ServletException("Error de base de datos", e);
        }
    }
}
