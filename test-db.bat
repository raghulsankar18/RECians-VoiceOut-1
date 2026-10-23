@echo off
echo ============================================================
echo   Testing RECian's VoiceOut JDBC Connection
echo ============================================================
cd /d "%~dp0"
powershell -NoProfile -Command "& java -cp 'WebContent\WEB-INF\classes;WebContent\WEB-INF\lib\*' com.recians.voiceout.test.TestDBConnection"
echo ============================================================
pause
