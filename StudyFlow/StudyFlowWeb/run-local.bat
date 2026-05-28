@echo off
cd /d "%~dp0\.."
call gradlew.bat :app:runWeb
pause
