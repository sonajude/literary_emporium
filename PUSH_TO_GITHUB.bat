@echo off
title Push Literary Emporium to GitHub
echo ========================================================
echo        Pushing Literary Emporium to GitHub
echo ========================================================
echo.
echo Remote: https://github.com/sonajude/literary_emporium.git
echo.
git push -u origin main
echo.
if %ERRORLEVEL% equ 0 (
    echo ========================================================
    echo SUCCESS: Project pushed to GitHub successfully!
    echo ========================================================
) else (
    echo ========================================================
    echo Push failed. Please check your GitHub login or connection.
    echo ========================================================
)
echo.
pause
