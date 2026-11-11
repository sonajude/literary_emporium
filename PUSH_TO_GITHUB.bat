@echo off
title Push Literary Emporium to GitHub
echo ========================================================
echo        Pushing Literary Emporium to GitHub
echo ========================================================
echo.
echo Remote Repository: https://github.com/sonajude/literary-emporium.git
echo Branch: main
echo.
echo Running: git push origin main
git push origin main
echo.
if %ERRORLEVEL% equ 0 (
    echo ========================================================
    echo SUCCESS: Project pushed to GitHub successfully!
    echo ========================================================
) else (
    echo ========================================================
    echo If GitHub asked for authentication, please sign in.
    echo If using a Personal Access Token (PAT), run:
    echo git push https://YOUR_TOKEN@github.com/sonajude/literary-emporium.git main
    echo ========================================================
)
echo.
pause
