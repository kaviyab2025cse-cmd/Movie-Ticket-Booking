@echo off
title Push CineBook to GitHub
echo ============================================================
echo   Pushing CineBook to GitHub Repository:
echo   https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking
echo ============================================================
echo.
set GIT_EXE=C:\Users\kaviy\AppData\Local\Microsoft\WinGet\Packages\Git.MinGit_Microsoft.Winget.Source_8wekyb3d8bbwe\cmd\git.exe

echo Enter your GitHub Personal Access Token (PAT).
echo (To create one: GitHub.com - Settings - Developer Settings - Personal Access Tokens - Generate Token)
set /p TOKEN="Paste your GitHub Token: "

if "%TOKEN%"=="" (
    echo [ERROR] Token cannot be empty.
    pause
    exit /b 1
)

echo.
echo Pushing to GitHub...
"%GIT_EXE%" push https://%TOKEN%@github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking.git main

if %errorlevel% equ 0 (
    echo.
    echo ============================================================
    echo [SUCCESS] Project pushed successfully to your GitHub repository!
    echo Check it at: https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking
    echo ============================================================
) else (
    echo.
    echo [ERROR] Push failed. Please verify your token has 'repo' write permissions.
)
pause
