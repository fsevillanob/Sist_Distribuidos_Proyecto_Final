# StudyApp

Aplicación web de escritorio para estudiar, desarrollada en **Java 8** con **Tomcat 9** y **Derby embebida**. Incluye sistema de login con roles, panel de administración de usuarios y temporizador Pomodoro.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Servidor | Apache Tomcat 9 |
| Lenguaje backend | Java 8 (Servlets) |
| Base de datos | Apache Derby (embebida) |
| Frontend | HTML5 + Bootstrap 5.3 + Bootstrap Icons |
| Estilos | CSS3 custom (glassmorphism) |

---

## Estructura del proyecto

```
mi-app/
├── deploy.bat               # Compila y despliega en Tomcat
├── index.html               # Redirige automáticamente al login
├── login.html               # Formulario de login
├── pomodoro.html            # Temporizador Pomodoro (standalone)
├── .gitignore
├── README.md
├── css/
│   └── style.css            # Estilos globales
├── WEB-INF/
│   └── web.xml              # Configuración de servlets y mapeo de URLs
└── src/
    ├── DBManager.java       # Conexión a Derby y CRUD de usuarios
    ├── LoginServlet.java    # GET: muestra login / POST: valida credenciales
    ├── LogoutServlet.java   # Invalida la sesión y redirige al login
    ├── MenuServlet.java     # Menú principal (generado dinámicamente)
    └── AdminServlet.java    # Panel de administración (solo rol admin)
```

---

## Requisitos

- JDK 1.8
- Apache Tomcat 9
- Derby incluido en el JDK (`jdk/db/lib/derby.jar`)

Se asume la siguiente estructura de instalación:

```
C:\Programs\JavaStack\
├── jdk1.8.0_131\
│   └── db\lib\derby.jar
└── apache-tomcat-9.0.89\
```

Si tu instalación es diferente, edita las variables al inicio del `deploy.bat`.

---

## Despliegue

1. Abre una ventana de comandos en la carpeta del proyecto
2. Ejecuta:
   ```bat
   deploy.bat
   ```
3. Accede en el navegador a:
   ```
   http://localhost:8082/mi-app
   ```

> El puerto puede variar según tu configuración de Tomcat. Comprueba el `server.xml` si no responde en el 8082.

---

## Usuarios por defecto

| Usuario | Contraseña | Rol |
|---|---|---|
| admin | admin123 | admin |
| usuario1 | 1234 | user |

> Las contraseñas se guardan en texto plano. Es una aplicación de demostración, no apta para producción.

---

## Funcionalidades

### Login
- Formulario de autenticación con sesión HTTP
- Redirección automática al menú si ya hay sesión activa
- Mensaje de error si las credenciales son incorrectas

### Menú principal
- Generado dinámicamente por `MenuServlet` según el rol del usuario
- Los usuarios con rol `user` solo ven el botón de Pomodoro
- Los usuarios con rol `admin` ven además el botón del panel de administración

### Panel de administración (solo admin)
- Listar todos los usuarios registrados
- Crear nuevos usuarios con nombre, contraseña y rol
- Cambiar la contraseña de cualquier usuario
- Eliminar usuarios
- Acceso protegido: aunque se teclee la URL directamente, se comprueba el rol en el servidor

### Técnica Pomodoro
- Temporizador con tres modos: Enfoque, Pausa corta y Pausa larga
- Anillo SVG animado con progreso visual
- Configuración personalizable de duración de cada modo y número de sesiones por ciclo
- Historial de sesiones completadas durante la sesión actual
- Sonido de aviso al terminar cada sesión
- No requiere sesión activa, es un HTML completamente estático

---

## Base de datos

Derby se ejecuta embebida dentro del proceso de Tomcat. La base de datos se crea automáticamente en el primer arranque en el directorio de trabajo de Tomcat:

```
C:\Programs\JavaStack\apache-tomcat-9.0.89\studyapp_db\
```

### Tabla `usuarios`

| Campo | Tipo | Descripción |
|---|---|---|
| id | INTEGER (PK) | Autoincremental |
| username | VARCHAR(50) | Único |
| password | VARCHAR(100) | Texto plano |
| rol | VARCHAR(10) | `admin` o `user` |

---

## Flujo de una petición

```
Navegador                  Tomcat                      Java / Derby
─────────                  ──────                      ────────────
GET  /mi-app          →    index.html (meta refresh)
GET  /login           →    login.html (estático)
POST /login           →    LoginServlet.doPost()   →   DBManager.validarLogin()
                           crea HttpSession
GET  /menu            →    MenuServlet.doGet()     →   lee sesión → genera HTML
GET  /admin           →    AdminServlet.doGet()    →   DBManager.getUsuarios()
POST /admin           →    AdminServlet.doPost()   →   DBManager.crear/borrar/etc.
GET  /logout          →    LogoutServlet           →   session.invalidate()
GET  /pomodoro.html   →    pomodoro.html (estático)
```

---

## Lo que NO se commitea

Ver `.gitignore`. En resumen:

- `WEB-INF/classes/` — se regenera al compilar
- *`WEB-INF/lib/` — el `derby.jar` se copia en el deploy*
- `studyapp_db/` — la base de datos local
- `derby.log` — log automático de Derby