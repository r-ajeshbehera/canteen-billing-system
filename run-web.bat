@echo off
REM Compile and start the WEB version of the Smart Canteen Billing System
REM The browser opens by itself (http://localhost:8090, or the next free port)
if not exist bin mkdir bin
if not exist sales.txt copy sample-data\sales.txt sales.txt >nul
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
if errorlevel 1 goto end
java -cp bin com.rajesh.canteen.web.WebServer
:end
del sources.txt
