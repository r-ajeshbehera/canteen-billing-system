@echo off
REM Compile and run the Smart Canteen Billing System
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
if errorlevel 1 goto end
java -cp bin com.rajesh.canteen.Main
:end
del sources.txt
