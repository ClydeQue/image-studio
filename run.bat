@echo off
REM Patakbuhin ang app
REM Opsyonal na image
cd /d "%~dp0"

if not exist classes mkdir classes
javac -d classes src\imagestudio\*.java src\imagestudio\core\*.java ^
                 src\imagestudio\io\*.java src\imagestudio\ui\*.java ^
                 src\imagestudio\verify\*.java
if errorlevel 1 exit /b 1

set CP=classes
if exist lib\flatlaf.jar set CP=classes;lib\flatlaf.jar

java -cp %CP% imagestudio.ImageStudio %*
