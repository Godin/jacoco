#!/bin/sh

java \
    -jar /Users/godin/projects/jacoco/jacoco/org.jacoco.cli/target/org.jacoco.cli-0.8.13-SNAPSHOT-nodeps.jar \
    report \
    ./sonar-kotlin-checks/build/jacoco/test.exec \
    --classfiles ./sonar-kotlin-checks/build/classes/kotlin/main \
    --sourcefiles ./sonar-kotlin-checks/src/main/java \
    --text jacoco.txt
