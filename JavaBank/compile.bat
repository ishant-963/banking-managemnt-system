@echo off
if not exist out mkdir out
powershell -NoProfile -Command "javac -d out (Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName)"
echo Done.
