@echo off
setlocal
set "SCRIPT_DIR=%~dp0"

if exist "%SCRIPT_DIR%JavaBank" (
    cd /d "%SCRIPT_DIR%JavaBank"
) else (
    cd /d "%SCRIPT_DIR%"
)

set "CP=out;lib/*;lib/mysql-connector-j-26.10.0/*"
java -cp "%CP%" com.javabank.BankApp
