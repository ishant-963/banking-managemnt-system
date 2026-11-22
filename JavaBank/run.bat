@echo off
setlocal
cd /d "%~dp0"

set "CP=out;lib/*;lib/mysql-connector-j-26.10.0/*"
java -cp "%CP%" com.javabank.BankApp
