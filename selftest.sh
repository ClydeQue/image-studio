#!/bin/sh
# Suriin ang app
set -e
cd "$(dirname "$0")"
mkdir -p classes
javac -d classes $(find src -name '*.java')
java -cp classes imagestudio.verify.SelfTest
