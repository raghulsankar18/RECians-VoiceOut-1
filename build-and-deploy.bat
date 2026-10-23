@echo off
echo ============================================================
echo   Building and Deploying RECian's VoiceOut to Tomcat
echo ============================================================

set "PROJECT_DIR=%~dp0"
set "TOMCAT_DIR=C:\Users\navan\Downloads\apache-tomcat-9.0.123-windows-x64\apache-tomcat-9.0.123"

cd /d "%PROJECT_DIR%"
if not exist "WebContent\WEB-INF\classes" mkdir "WebContent\WEB-INF\classes"

echo Compiling Java source files...
powershell -NoProfile -Command "$cp = '%TOMCAT_DIR%\lib\servlet-api.jar;WebContent\WEB-INF\lib\*'; $files = (Get-ChildItem -Path 'src' -Filter '*.java' -Recurse).FullName; if ($files) { & javac -encoding UTF-8 -cp $cp -d 'WebContent\WEB-INF\classes' $files; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE } }"
if errorlevel 1 (
    echo Compilation failed! Please check Java errors above.
    exit /b 1
)
echo Java compilation succeeded!

:: Copy properties files to classes directory
robocopy "src" "WebContent\WEB-INF\classes" *.properties /S /NFL /NDL /NJH /NJS >nul 2>&1

echo Syncing WebContent to Tomcat webapps...
robocopy "WebContent" "%TOMCAT_DIR%\webapps\ReciansVoiceOut" /E /NFL /NDL /NJH /NJS

echo ============================================================
echo Deployment Complete!
echo You can view the application at:
echo http://localhost:8080/ReciansVoiceOut/
echo ============================================================
