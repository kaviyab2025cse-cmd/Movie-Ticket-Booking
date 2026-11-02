@echo off
title CineBook - Automated Test Suite
echo ========================================================
echo   Running CineBook OOP & DBMS Automated Tests
echo ========================================================
if not exist "bin" mkdir bin
javac -cp "lib\mysql-connector-j-8.4.0.jar;src;test" -d bin src\com\cinebook\model\*.java src\com\cinebook\exception\*.java src\com\cinebook\dao\*.java src\com\cinebook\service\*.java src\com\cinebook\app\*.java test\com\cinebook\test\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)
echo.
echo [1/2] Running Core OOP Test Suite (15 Test Cases)...
java -cp "bin;lib\mysql-connector-j-8.4.0.jar" com.cinebook.test.CineBookTest
echo.
echo [2/2] Running DBMS & CRUD Test Suite (5 Test Cases)...
java -cp "bin;lib\mysql-connector-j-8.4.0.jar" com.cinebook.test.DatabaseCRUDTest
echo.
echo All tests finished.
pause
