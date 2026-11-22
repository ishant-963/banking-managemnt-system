@echo off
setlocal
set "SCRIPT_DIR=%~dp0"

if exist "%SCRIPT_DIR%JavaBank" (
    cd /d "%SCRIPT_DIR%JavaBank"
) else (
    cd /d "%SCRIPT_DIR%"
)

if not exist out mkdir out
powershell -NoProfile -Command "javac -d out (Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName)"
echo Done.
