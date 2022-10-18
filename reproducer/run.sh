#!/bin/bash

gradle b
java \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED \
  --add-opens=java.sql/java.sql=ALL-UNNAMED \
  -javaagent:/tmp/j/jacoco-0.8.8/lib/jacocoagent.jar=output=none,classdumpdir=dump,inclnolocationclasses=true \
  -jar example/build/libs/example-all.jar
