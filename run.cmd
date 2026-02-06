@echo off
REM ===========================================
REM Script de Ejecucion - API Calculadora Tenpo
REM Autor: Abisaid Gomez
REM ===========================================
setlocal

REM Configurar Java 21
set JAVA_HOME=C:\Users\57310\.jdks\corretto-21.0.6
set PATH=%JAVA_HOME%\bin;%PATH%

echo ============================================
echo  API CALCULADORA - Script de Ejecucion
echo ============================================
echo.
echo Version de Java:
"%JAVA_HOME%\bin\java" -version
echo.

REM Verificar si Maven esta disponible
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven no esta en el PATH
    exit /b 1
)

echo.
echo IMPORTANTE: Asegurate de tener PostgreSQL corriendo en localhost:5432
echo Puedes iniciar PostgreSQL con:
echo   docker run -d --name postgres-calculator -e POSTGRES_DB=calculator_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine
echo.
echo Iniciando aplicacion...
echo.
echo API disponible en: http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.

cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dexternal-service.mock.enabled=true"

endlocal
