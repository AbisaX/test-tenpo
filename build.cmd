@echo off
REM ===========================================
REM Script de Compilacion - API Calculadora Tenpo
REM Autor: Abisaid Gomez
REM ===========================================
setlocal

REM Configurar Java 21
set JAVA_HOME=C:\Users\57310\.jdks\corretto-21.0.6
set PATH=%JAVA_HOME%\bin;%PATH%

echo ============================================
echo  API CALCULADORA - Script de Compilacion
echo ============================================
echo.
echo Version de Java:
"%JAVA_HOME%\bin\java" -version
echo.

REM Verificar si Maven esta disponible
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven no esta en el PATH
    echo Por favor instala Maven desde: https://maven.apache.org/download.cgi
    echo O usa Docker: docker-compose up --build
    exit /b 1
)

echo Compilando proyecto...
cd /d "%~dp0"
mvn clean compile -q

if %errorlevel% neq 0 (
    echo [ERROR] Error en la compilacion
    exit /b 1
)

echo.
echo [OK] Compilacion exitosa!
echo.

endlocal
