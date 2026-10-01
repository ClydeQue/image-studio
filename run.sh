#!/bin/sh
# Patakbuhin ang app
# Opsyonal na image
set -e
cd "$(dirname "$0")"

mkdir -p classes
javac -d classes $(find src -name '*.java')

CP="classes"
for jar in lib/*.jar; do
    [ -e "$jar" ] && CP="$CP:$jar"
done

java -cp "$CP" imagestudio.ImageStudio "$@"
