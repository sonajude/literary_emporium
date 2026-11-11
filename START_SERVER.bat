@echo off
title Literary Emporium - Server Launcher
echo ========================================================
echo        Starting Literary Emporium Server
echo ========================================================

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "JRE_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "CATALINA_HOME=C:\Users\ELCOT\OneDrive\Desktop\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60"

echo [1/3] Starting Apache Tomcat Server...
call "%CATALINA_HOME%\bin\catalina.bat" start

echo [2/3] Waiting for server initialization (6 seconds)...
timeout /t 6 /nobreak >nul

echo [3/3] Opening website in your default browser...
start "" "http://localhost:8080/literary-emporium/"

echo ========================================================
echo Server started!
echo URL: http://localhost:8080/literary-emporium/
echo Keep the Tomcat window open while testing.
echo ========================================================
pause
