@echo off
title Literary Emporium - Project Build and Package
echo ========================================================
echo        Building Literary Emporium WAR Package
echo ========================================================

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "CATALINA_HOME=C:\Users\ELCOT\OneDrive\Desktop\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60"

echo [1/5] Synchronizing web application assets...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Path . -File | Where-Object { $_.Name -match '\.(html|js|css)$' } | ForEach-Object { Copy-Item -Path $_.FullName -Destination 'Backend\src\main\webapp\' -Force }; Copy-Item -Path 'images\*' -Destination 'Backend\src\main\webapp\images\' -Force"

echo [2/5] Compiling Java classes with JDK 21...
if not exist "Backend\target\classes" mkdir "Backend\target\classes"
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%JAVA_HOME%\bin\javac.exe' -cp 'Backend\target\literary-emporium\WEB-INF\lib\*;Backend\lib\*;%CATALINA_HOME%\lib\*' -d 'Backend\target\classes' (Get-ChildItem -Path 'Backend\src\main\java' -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName)"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [3/5] Updating target exploded directory...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Copy-Item -Path 'Backend\target\classes\*' -Destination 'Backend\target\literary-emporium\WEB-INF\classes\' -Recurse -Force; Copy-Item -Path 'Backend\src\main\webapp\*' -Destination 'Backend\target\literary-emporium\' -Recurse -Force"

echo [4/5] Packaging literary-emporium.war...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Set-Location 'Backend\target\literary-emporium'; & '%JAVA_HOME%\bin\jar.exe' -cvf '..\literary-emporium.war' * > $null; Set-Location '..\..\..'"

echo [5/5] Deploying WAR into Apache Tomcat webapps...
if exist "%CATALINA_HOME%\webapps" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Copy-Item -Path 'Backend\target\literary-emporium.war' -Destination '%CATALINA_HOME%\webapps\literary-emporium.war' -Force; if (Test-Path '%CATALINA_HOME%\webapps\literary-emporium') { Remove-Item -Path '%CATALINA_HOME%\webapps\literary-emporium' -Recurse -Force }"
    echo Deployment completed successfully!
) else (
    echo [NOTE] Tomcat webapps folder not found at %CATALINA_HOME%\webapps. WAR generated at Backend\target\literary-emporium.war.
)

echo ========================================================
echo Build Succeeded! You can now run START_SERVER.bat.
echo ========================================================
