#!/bin/sh

# Set JavaHome if it exists
if [ -f "${JAVA_HOME}/bin/java" ]; then 
   JAVA=${JAVA_HOME}/bin/java
else
   JAVA=java
fi
export JAVA

PRG="$0"

SAMPLEDB_HOME=`dirname "$PRG"`
export SAMPLEDB_HOME

"$JAVA" -server -cp ".:$SAMPLEDB_HOME/lib/*" com.bio.sample.Application $*
