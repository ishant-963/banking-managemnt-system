#!/bin/sh
# Compiles every .java file in src/ into the out/ folder
mkdir -p out
javac -d out $(find src -name "*.java") && echo "Compiled successfully."
