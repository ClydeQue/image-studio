#!/bin/sh
# Compiles and runs the verification harness. Prints a PASS / FAIL table and
# regenerates test-images/color-chart.png plus everything in outputs/.
set -e
cd "$(dirname "$0")"
mkdir -p classes
javac -d classes $(find src -name '*.java')
java -cp classes imagestudio.verify.SelfTest
