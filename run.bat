@echo off
title CineBook - Movie Ticket Booking System
echo ========================================================
echo   CineBook - Movie Ticket Booking System (OOPs & DBMS)
echo ========================================================
echo Compiling source code...
if not exist "bin" mkdir bin
javac -cp "lib\mysql-connector-j-8.4.0.jar;src" -d bin src\com\cinebook\model\*.java src\com\cinebook\exception\*.java src\com\cinebook\dao\*.java src\com\cinebook\service\*.java src\com\cinebook\app\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ is installed and on your PATH.
    pause
    exit /b %errorlevel%
)
echo Launching CineBook Console Application...
java -cp "bin;lib\mysql-connector-j-8.4.0.jar" com.cinebook.app.CineBook
pause
