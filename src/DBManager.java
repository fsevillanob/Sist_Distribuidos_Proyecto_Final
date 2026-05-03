import java.sql.*;

/**
 * Gestor de base de datos Derby embebida.
 * Crea las tablas y el usuario admin por defecto si no existen.
 */
public class DBManager {

    // Derby embebida: la BD se crea en el directorio de trabajo de Tomcat
    private static final String DB_URL = "jdbc:derby:studyapp_db;create=true";

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            initDB();
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar Derby: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void initDB() throws SQLException {
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {

            // Crear tabla de usuarios si no existe
            try {
                st.executeUpdate(
                    "CREATE TABLE usuarios (" +
                    "  id       INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  username VARCHAR(50)  NOT NULL UNIQUE," +
                    "  password VARCHAR(100) NOT NULL," +
                    "  rol      VARCHAR(10)  NOT NULL DEFAULT 'user'" +
                    ")"
                );
                System.out.println("[StudyApp] Tabla 'usuarios' creada.");
                // Insertar admin por defecto
                st.executeUpdate(
                    "INSERT INTO usuarios (username, password, rol) VALUES ('admin', 'admin123', 'admin')"
                );
                st.executeUpdate(
                    "INSERT INTO usuarios (username, password, rol) VALUES ('usuario1', '1234', 'user')"
                );
                System.out.println("[StudyApp] Usuarios por defecto creados.");

            } catch (SQLException e) {
                // La tabla ya existe (código X0Y32 en Derby) — ignoramos
                if (!"X0Y32".equals(e.getSQLState())) {
                    throw e;
                }
                System.out.println("[StudyApp] Tabla 'usuarios' ya existe.");
            }
            
            // crear tabla de tareas si no existe
            try {
                st.executeUpdate(
                    "CREATE TABLE tareas (" +
                    "  id_tarea    INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  id_usuario  INTEGER NOT NULL," +
                    "  nombre      VARCHAR(100) NOT NULL," +
                    "  descripcion VARCHAR(255)," +
                    "  fecha_hora  TIMESTAMP NOT NULL," +
                    "  estado      VARCHAR(20) NOT NULL," +
                    "  CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE" +
                    ")"
                );
                System.out.println("[StudyApp] Tabla 'tareas' creada con éxito.");
            } catch (SQLException e) {
                if (!"X0Y32".equals(e.getSQLState())) throw e;
            }
        }
    }

    // ─── CRUD de usuarios ───────────────────────────────────────────────────

    /** Valida credenciales. Devuelve el rol si son correctas, null si no. */
    public static String validarLogin(String username, String password) throws SQLException {
        String sql = "SELECT rol FROM usuarios WHERE username=? AND password=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("rol");
            }
        }
        return null;
    }

    /** Devuelve todos los usuarios (sin la contraseña). */
    public static ResultSet getUsuarios(Connection con) throws SQLException {
        Statement st = con.createStatement();
        return st.executeQuery("SELECT id, username, rol FROM usuarios ORDER BY id");
    }

    /** Crea un nuevo usuario. */
    public static void crearUsuario(String username, String password, String rol) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password, rol) VALUES (?,?,?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, rol);
            ps.executeUpdate();
        }
    }

    /** Cambia la contraseña de un usuario por su id. */
    public static void cambiarPassword(int id, String newPassword) throws SQLException {
        String sql = "UPDATE usuarios SET password=? WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Elimina un usuario por su id. */
    public static void eliminarUsuario(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

        public static int getUsuarioId(String username) throws SQLException {
        String sql = "SELECT id FROM usuarios WHERE username = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    public static void crearTarea(int idUsuario, String nombre, String desc, String ts, String estado) throws SQLException {
        String sql = "INSERT INTO tareas (id_usuario, nombre, descripcion, fecha_hora, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, nombre);
            ps.setString(3, desc);
            ps.setTimestamp(4, Timestamp.valueOf(ts.replace("T", " ") + ":00"));
            ps.setString(5, estado);
            ps.executeUpdate();
        }
    }

    public static void actualizarEstadoTarea(int idTarea, String nuevoEstado) throws SQLException {
        String sql = "UPDATE tareas SET estado = ? WHERE id_tarea = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idTarea);
            ps.executeUpdate();
        }
    }

    public static ResultSet listarTareasUsuario(int idUsuario, String orden, String search) throws SQLException {
        Connection con = getConnection();
        String criteria = "fecha_hora ASC"; // Por defecto
        if ("estado".equals(orden)) criteria = "estado DESC";
        
        // Construimos la consulta base
        String sql = "SELECT * FROM tareas WHERE id_usuario = ?";
        
        // Si hay un término de búsqueda, lo añadimos a la consulta (ignorando mayúsculas/minúsculas)
        if (search != null && !search.trim().isEmpty()) {
            sql += " AND LOWER(nombre) LIKE LOWER(?)";
        }
        
        sql += " ORDER BY " + criteria;
        
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idUsuario);
        
        // Asignamos el parámetro de búsqueda si existe
        if (search != null && !search.trim().isEmpty()) {
            ps.setString(2, "%" + search.trim() + "%");
        }
        
        return ps.executeQuery();
    }

    public static void eliminarTarea(int idTarea, int idUsuario) throws SQLException {
        String sql = "DELETE FROM tareas WHERE id_tarea = ? AND id_usuario = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTarea);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }
}
