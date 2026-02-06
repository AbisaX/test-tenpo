@echo off
REM ===========================================
REM Script de Tests - API Calculadora Tenpo
REM Autor: Abisaid Gomez
REM ===========================================
setlocal

REM Configurar Java 21
set JAVA_HOME=C:\Users\57310\.jdks\corretto-21.0.6
set PATH=%JAVA_HOME%\bin;%PATH%

echo ============================================
echo  API CALCULADORA - Script de Tests
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

echo Ejecutando tests unitarios...
cd /d "%~dp0"
mvn test -Dspring.profiles.active=test

if %errorlevel% neq 0 (
    echo [ERROR] Algunos tests fallaron
    exit /b 1
)

echo.
echo [OK] Todos los tests pasaron!
echo.

endlocal
