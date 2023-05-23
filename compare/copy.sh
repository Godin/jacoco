#!/bin/sh

cp ~/projects/jacoco/jacoco/org.jacoco.agent/target/org.jacoco.agent-0.8.11-SNAPSHOT.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.agent.rt/target/org.jacoco.agent.rt-0.8.11-SNAPSHOT.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.agent.rt/target/org.jacoco.agent.rt-0.8.11-SNAPSHOT-all.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.core/target/org.jacoco.core-0.8.11-SNAPSHOT.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.report/target/org.jacoco.report-0.8.11-SNAPSHOT.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.ant/target/org.jacoco.ant-0.8.11-SNAPSHOT.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.ant/target/org.jacoco.ant-0.8.11-SNAPSHOT-nodeps.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.cli/target/org.jacoco.cli-0.8.11-SNAPSHOT.jar .
cp ~/projects/jacoco/jacoco/org.jacoco.cli/target/org.jacoco.cli-0.8.11-SNAPSHOT-nodeps.jar .
cp ~/projects/jacoco/jacoco/jacoco/target/jacoco-*.zip .

aunpack org.jacoco.agent-0.8.11-SNAPSHOT.jar
aunpack org.jacoco.agent.rt-0.8.11-SNAPSHOT.jar
aunpack org.jacoco.agent.rt-0.8.11-SNAPSHOT-all.jar
aunpack org.jacoco.core-0.8.11-SNAPSHOT.jar
aunpack org.jacoco.report-0.8.11-SNAPSHOT.jar
aunpack org.jacoco.ant-0.8.11-SNAPSHOT.jar
aunpack org.jacoco.ant-0.8.11-SNAPSHOT-nodeps.jar
aunpack org.jacoco.cli-0.8.11-SNAPSHOT.jar
aunpack org.jacoco.cli-0.8.11-SNAPSHOT-nodeps.jar
