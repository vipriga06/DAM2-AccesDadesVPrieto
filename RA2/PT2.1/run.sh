#!/bin/bash

# run.sh

# Change the working directory to where the script is located
cd "$(dirname "$0")"

# Set MAVEN_OPTS environment variable
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"

# Check for the first argument and set it as the main class
mainClass="$1"

echo "Setting MAVEN_OPTS to: $MAVEN_OPTS"
echo "Main Class: $mainClass"

# Execute mvn command with the profile and main class as arguments
mvn clean compile exec:java -PrunMain -Dexec.mainClass="$mainClass"
