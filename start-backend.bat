@echo off
title Student Project Portal - Backend Server
echo ===================================================
echo   Starting Spring Boot 3 Backend Server
echo ===================================================
echo.
cd /d "%~dp0"
call mvnw.cmd spring-boot:run
pause
