@echo off
REM === 1. CONFIGURACIÓN DE ENTORNO ===
set THIS_DRIVE=%cd:~0,2%
set JAVA_STACK=%THIS_DRIVE%\Programs\JavaStack
set JAVA_HOME=%JAVA_STACK%\jdk1.8.0_131
set CATALINA_HOME=%JAVA_STACK%\apache-tomcat-9.0.89
set DERBY_HOME=%JAVA_HOME%\db

REM Nombre de tu carpeta en Tomcat (como se verá en la URL)
set APP_NAME=mi-app

REM Actualizamos el PATH y CLASSPATH para esta sesión
set PATH=%JAVA_HOME%\bin;%DERBY_HOME%\bin;%CATALINA_HOME%\bin;%PATH%
set CLASSPATH=.;%CATALINA_HOME%\lib\servlet-api.jar;%DERBY_HOME%\lib\derby.jar;%DERBY_HOME%\lib\derbynet.jar;%DERBY_HOME%\lib\derbyclient.jar

echo [1/4] Cerrando Tomcat (si estaba abierto)...
call shutdown.bat >nul 2>&1
taskkill /f /im java.exe >nul 2>&1
timeout /t 2 >nul

echo [2/4] Limpiando y Compilando Java...
if not exist "WEB-INF\classes" mkdir "WEB-INF\classes"

javac -d WEB-INF/classes src/*.java
if %errorlevel% neq 0 (
    echo ERROR DE COMPILACION. Revisa tu codigo.
    pause
    exit /b
)

echo [3/4] Desplegando en Tomcat...
rd /s /q "%CATALINA_HOME%\webapps\%APP_NAME%" 2>nul
mkdir "%CATALINA_HOME%\webapps\%APP_NAME%"

REM copiar librerias necesarias 
copy "%DERBY_HOME%\lib\derby.jar" "%CATALINA_HOME%\webapps\%APP_NAME%\WEB-INF\lib\"

REM Copiar HTML raíz
xcopy /s /y "index.html"    "%CATALINA_HOME%\webapps\%APP_NAME%\"
xcopy /s /y "login.html"    "%CATALINA_HOME%\webapps\%APP_NAME%\"
xcopy /s /y "pomodoro.html" "%CATALINA_HOME%\webapps\%APP_NAME%\"

REM Copiar WEB-INF (clases compiladas + web.xml)
xcopy /s /y "WEB-INF" "%CATALINA_HOME%\webapps\%APP_NAME%\WEB-INF\"

REM Copiar carpeta CSS
xcopy /s /y "css" "%CATALINA_HOME%\webapps\%APP_NAME%\css\" 2>nul

echo [4/4] Arrancando Tomcat...
echo.
echo  Aplicacion desplegada en: http://localhost:8080/%APP_NAME%
echo  Login por defecto -> admin / admin123
echo.
call catalina.bat run
