import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CalendarioEventosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        String username = (String) session.getAttribute("username");

        try {
            int userId = DBManager.getUsuarioId(username);
            ResultSet rs = DBManager.listarTareasUsuario(userId, "fecha", null);

            out.print("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) out.print(",");
                String estado = rs.getString("estado");
                String color  = estado.equals("Terminada")  ? "#198754" :
                                estado.equals("En proceso") ? "#fd7e14" : "#6c757d";

                out.print("{");
                out.print("\"id\":\""               + rs.getInt("id_tarea")                                         + "\",");
                out.print("\"title\":\""             + rs.getString("nombre").replace("\"", "\\\"")                  + "\",");
                out.print("\"start\":\""             + rs.getTimestamp("fecha_hora").toString().replace(" ", "T")    + "\",");
                out.print("\"backgroundColor\":\"" + color                                                           + "\",");
                out.print("\"borderColor\":\""      + color                                                           + "\",");
                out.print("\"extendedProps\":{"      );
                out.print("  \"desc\":\""            + (rs.getString("descripcion") != null ? rs.getString("descripcion").replace("\"", "\\\"") : "") + "\",");
                out.print("  \"status\":\""          + estado                                                         + "\"");
                out.print("}");
                out.print("}");
                first = false;
            }
            out.print("]");

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }
}