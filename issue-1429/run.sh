#!/bin/bash

set -e

KOTLIN=${HOME}/apps/kotlin/1.5.0
${KOTLIN}/bin/kotlinc -version
rm -rf classes
# TODO not reproducable with -jvm-target 1.6
${KOTLIN}/bin/kotlinc -d classes -jvm-target 1.8 Example.kt

java \
    -cp ${KOTLIN}/lib/kotlin-stdlib.jar:classes example.ExampleKt

rm -rf instrumented
java -jar ../org.jacoco.cli/target/org.jacoco.cli-*-SNAPSHOT-nodeps.jar instrument classes --dest instrumented

java -javaagent:../org.jacoco.agent.rt/target/org.jacoco.agent.rt-0.8.11-SNAPSHOT-all.jar=output=none,includes=example.ExampleKt \
    -cp ${KOTLIN}/lib/kotlin-stdlib.jar:classes example.ExampleKt
