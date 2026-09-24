#!/bin/sh
# Compiles and runs Image Studio.
# Picks up lib/flatlaf.jar for the modern look if it is there, and works fine
# if it is not. An optional image path is passed straight through:
#     ./run.sh test-images/color-chart.png
set -e
cd "$(dirname "$0")"

mkdir -p classes
javac -d classes $(find src -name '*.java')

CP="classes"
for jar in lib/*.jar; do
    [ -e "$jar" ] && CP="$CP:$jar"
done

java -cp "$CP" imagestudio.ImageStudio "$@"
