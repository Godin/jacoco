#!/bin/sh

~/apps/kotlin/2.0.0/bin/kotlinc -d classes/a src/a/A.kt
~/apps/kotlin/2.0.0/bin/kotlinc -cp ${HOME}/apps/kotlin/2.0.0/lib/kotlinx-coroutines-core-jvm.jar:classes/a -d classes/b src/b/B.kt
