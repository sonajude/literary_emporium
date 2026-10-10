@echo off
title Literary Emporium - Server Shutdown
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "JRE_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "CATALINA_HOME=C:\Users\ELCOT\OneDrive\Desktop\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60"

echo Stopping Tomcat server...
call "%CATALINA_HOME%\bin\shutdown.bat"
echo Tomcat server stopped.
