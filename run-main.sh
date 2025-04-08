#!/bin/bash

# Get the full classpath including dependencies from the file we generated
CP="target/classes:$(cat .classpath)"

# Run the main class with all dependencies
java -cp "$CP" com.mycompany.myapp.MsMediaApp 