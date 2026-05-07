# StudyApp
 
Aplicación web de escritorio para estudiar, desarrollada en **Java 8** con **Tomcat 9** y **Derby embebida**. Incluye sistema de login con roles, panel de administración de usuarios, gestión de tareas con calendario y temporizador Pomodoro.
 
---
 
## Tecnologías
 
| Capa | Tecnología |
|---|---|
| Servidor | Apache Tomcat 9 |
| Lenguaje backend | Java 8 (Servlets) |
| Base de datos | Apache Derby (embebida) |
| Frontend | HTML5 + Bootstrap 5.3 + Bootstrap Icons |
| Calendario | FullCalendar 6.1 |
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
    ├── DBManager.java              # Conexión a Derby y CRUD de usuarios y tareas
    ├── LoginServlet.java           # GET: muestra login / POST: valida credenciales
    ├── LogoutServlet.java          # Invalida la sesión y redirige al login
    ├── MenuServlet.java            # Menú principal (generado dinámicamente)
    ├── AdminServlet.java           # Panel de administración: muestra la tabla de usuarios
    ├── CrearUsuarioServlet.java    # POST: crea un usuario nuevo
    ├── CambiarPasswordServlet.java # POST: cambia la contraseña de un usuario
    ├── EliminarUsuarioServlet.java # POST: elimina un usuario
    ├── TareaServlet.java           # GET: lista las tareas del usuario
    ├── CrearTareaServlet.java      # POST: crea una tarea nueva
    ├── ActualizarTareaServlet.java # POST: cambia el estado de una tarea
    ├── EliminarTareaServlet.java   # POST: elimina una tarea
    └── CalendarioServlet.java      # GET: vista calendario / GET?action=events: JSON para FullCalendar
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
- Los usuarios con rol `user` ven Pomodoro, Gestión de Tareas y Calendario
- Los usuarios con rol `admin` ven además el botón del panel de administración
### Panel de administración (solo admin)
- Listar todos los usuarios registrados
- Crear nuevos usuarios con nombre, contraseña y rol
- Cambiar la contraseña de cualquier usuario
- Eliminar usuarios
- Acceso protegido: aunque se teclee la URL directamente, se comprueba el rol en el servidor
- Cada acción tiene su propio servlet (`/admin/crear`, `/admin/password`, `/admin/eliminar`)
### Gestión de tareas
- Listar todas las tareas del usuario logueado
- Crear tareas con nombre, descripción, fecha/hora y estado inicial
- Cambiar el estado de una tarea: Pendiente, En proceso, Terminada
- Eliminar tareas
- Cada acción tiene su propio servlet (`/tareas/crear`, `/tareas/actualizar`, `/tareas/eliminar`)
### Calendario
- Visualización de las tareas del usuario en vista mensual, semanal o agenda
- Los colores indican el estado: verde (Terminada), naranja (En proceso), gris (Pendiente)
- Al hacer clic en un evento se muestra el nombre, estado y descripción de la tarea
- `CalendarioServlet` tiene doble modo: sirve el HTML de la página o devuelve JSON para FullCalendar según el parámetro `?action=events`
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
 
### Tabla `tareas`
 
| Campo | Tipo | Descripción |
|---|---|---|
| id_tarea | INTEGER (PK) | Autoincremental |
| id_usuario | INTEGER (FK) | Referencia a `usuarios.id` |
| nombre | VARCHAR(100) | Nombre de la tarea |
| descripcion | VARCHAR(255) | Descripción opcional |
| fecha_hora | TIMESTAMP | Fecha y hora de la tarea |
| estado | VARCHAR(20) | `Pendiente`, `En proceso` o `Terminada` |
 
---
 
## Mapeo de URLs
 
| URL | Servlet | Método | Descripción |
|---|---|---|---|
| `/login` | LoginServlet | GET | Muestra el formulario de login |
| `/login` | LoginServlet | POST | Valida credenciales y crea sesión |
| `/logout` | LogoutServlet | GET | Cierra la sesión |
| `/menu` | MenuServlet | GET | Menú principal |
| `/admin` | AdminServlet | GET | Panel de administración |
| `/admin/crear` | CrearUsuarioServlet | POST | Crea un usuario |
| `/admin/password` | CambiarPasswordServlet | POST | Cambia contraseña |
| `/admin/eliminar` | EliminarUsuarioServlet | POST | Elimina un usuario |
| `/tareas` | TareaServlet | GET | Lista de tareas |
| `/tareas/crear` | CrearTareaServlet | POST | Crea una tarea |
| `/tareas/actualizar` | ActualizarTareaServlet | POST | Cambia estado de tarea |
| `/tareas/eliminar` | EliminarTareaServlet | POST | Elimina una tarea |
| `/calendario` | CalendarioServlet | GET | Vista de calendario |
| `/calendario?action=events` | CalendarioServlet | GET | JSON de eventos para FullCalendar |
| `/pomodoro.html` | — | GET | Temporizador Pomodoro (estático) |
 
---
 
## Flujo de una petición
 
```
Navegador                            Tomcat                         Java / Derby
---------                            ------                         ------------
GET  /mi-app                    →    index.html (meta refresh)
GET  /login                     →    login.html (estático)
POST /login                     →    LoginServlet.doPost()      →   DBManager.validarLogin()
GET  /menu                      →    MenuServlet.doGet()        →   lee sesión, genera HTML
GET  /admin                     →    AdminServlet.doGet()       →   DBManager.getUsuarios()
POST /admin/crear               →    CrearUsuarioServlet        →   DBManager.crearUsuario()
POST /admin/password            →    CambiarPasswordServlet     →   DBManager.cambiarPassword()
POST /admin/eliminar            →    EliminarUsuarioServlet     →   DBManager.eliminarUsuario()
GET  /tareas                    →    TareaServlet.doGet()       →   DBManager.listarTareas()
POST /tareas/crear              →    CrearTareaServlet          →   DBManager.crearTarea()
POST /tareas/actualizar         →    ActualizarTareaServlet     →   DBManager.actualizarEstado()
POST /tareas/eliminar           →    EliminarTareaServlet       →   DBManager.eliminarTarea()
GET  /calendario                →    CalendarioServlet          →   genera HTML con FullCalendar
GET  /calendario?action=events  →    CalendarioServlet          →   DBManager.listarTareas() → JSON
GET  /logout                    →    LogoutServlet              →   session.invalidate()
GET  /pomodoro.html             →    pomodoro.html (estático)
```
 
---