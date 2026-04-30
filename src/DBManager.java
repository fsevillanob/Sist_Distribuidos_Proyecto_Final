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
}
