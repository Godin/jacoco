#!/bin/sh

rm -rf cls_v1
javac src_v1/*.java -d cls_v1

rm -rf cls_v2
javac src_v2/*.java -d cls_v2

java -javaagent:jacoco-0.8.4/lib/jacocoagent.jar=destfile=v1.exec -cp cls_v1 Main
java -javaagent:jacoco-0.8.4/lib/jacocoagent.jar=destfile=v2.exec -cp cls_v2 Main

java -jar jacoco-0.8.4/lib/jacococli.jar \
     report v1.exec \
     --classfiles cls_v1 \
     --sourcefiles src_v1 \
     --html v1 \
     --xml v1/jacoco.xml

java -jar jacoco-0.8.4/lib/jacococli.jar \
     report v2.exec \
     --classfiles cls_v2 \
     --sourcefiles src_v2 \
     --html v2 \
     --xml v2/jacoco.xml
