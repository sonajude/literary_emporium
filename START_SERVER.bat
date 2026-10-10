@echo off
title Literary Emporium - Server Launcher
echo ========================================================
echo        Starting Literary Emporium Server
echo ========================================================

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "JRE_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "CATALINA_HOME=C:\Users\ELCOT\OneDrive\Desktop\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60"

echo [1/3] Starting Apache Tomcat Server...
start "Tomcat Server" /D "%CATALINA_HOME%\bin" "%CATALINA_HOME%\bin\catalina.bat" run

echo [2/3] Waiting for server initialization (4 seconds)...
timeout /t 4 /nobreak >nul

echo [3/3] Opening website in your default browser...
start http://localhost:8080/literary-emporium/

echo ========================================================
echo Server started! Keep the Tomcat window open while testing.
echo ========================================================
