@echo off
echo ====================================
echo  STARTING BANK ACCOUNT APPLICATION
echo ====================================
echo.
echo Port: 9090
echo H2 Console: http://localhost:9090/h2-console
echo JDBC URL: jdbc:h2:mem:bankdb
echo.
echo Starting...
echo.

cd /d "%~dp0"
call mvn spring-boot:run

pause
