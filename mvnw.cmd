@REM ===========================================
@REM Maven Wrapper para Windows - API Calculadora Tenpo
@REM Autor: Abisaid Gomez
@REM ===========================================
@REM Este script permite ejecutar Maven sin instalarlo globalmente.
@REM Uso: mvnw.cmd [comando] (ejemplo: mvnw.cmd clean install)
@REM ===========================================

@echo off
setlocal

REM Configurar Java 21
set JAVA_HOME=C:\Users\57310\.jdks\corretto-21.0.6
set PATH=%JAVA_HOME%\bin;%PATH%
set MAVEN_HOME=%~dp0apache-maven-3.9.6

echo.
echo ============================================
echo  API Calculadora - Maven Wrapper
echo ============================================
echo  Java: %JAVA_HOME%
echo  Maven: %MAVEN_HOME%
echo ============================================
echo.

call "%MAVEN_HOME%\bin\mvn.cmd" %*

endlocal
