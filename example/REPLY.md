`kotlinx.coroutines.debug.internal.ByteBuddyDynamicAttach.attach` mentioned in your stacktrace [has following implementation](https://github.com/Kotlin/kotlinx.coroutines/blob/1.6.4/kotlinx-coroutines-debug/src/internal/Attach.kt#L20-L30):
```
        val cl = Class.forName("kotlin.coroutines.jvm.internal.DebugProbesKt")
        val cl2 = Class.forName("kotlinx.coroutines.debug.internal.DebugProbesKt")

        ByteBuddy()
            .redefine(cl2)
            .name(cl.name)
            .make()
            .load(cl.classLoader, ClassReloadingStrategy.fromInstalledAgent())
```

* `redefine(cl2)` loads bytes of class `kotlinx.coroutines.debug.internal.DebugProbesKt` **from the disk**
* `name(cl.name).make()` from the loaded bytes creates class named `kotlin.coroutines.jvm.internal.DebugProbesKt`
* `load(cl.classLoader, ClassReloadingStrategy.fromInstalledAgent())` **replaces already loaded by JVM class** `kotlinx.coroutines.debug.internal.DebugProbesKt` by the just created

ByteBuddy agent is after JaCoCo agent, so created replacement class loaded by ByteBuddy won't be instrumented by JaCoCo, whereas class being replaced is instrumented by JaCoCo. This behavior is also described in  https://github.com/raphw/byte-buddy/issues/1248

JaCoCo instrumentation adds field and method to the instrumented classes.

So with OpenJDK >= 13 because of https://bugs.openjdk.org/browse/JDK-8192936 all of this leads to
```
java.lang.UnsupportedOperationException: class redefinition failed: attempted to delete a method
```

To overcome this you can
* either use OpenJDK < 13, e.g. 11
* or with OpenJDK >= 13 use [JVM argument `-XX:+AllowRedefinitionToAddDeleteMethods`](https://bugs.openjdk.org/browse/JDK-8221528)
* or simply exclude involved classes from JaCoCo instrumentation
```
tasks.withType(Test) {
    jacoco.excludes = [
        'kotlin.coroutines.jvm.internal.DebugProbesKt',
        'kotlinx.coroutines.debug.internal.DebugProbesKt',
        'kotlinx.coroutines.debug.internal.NoOpProbesKt',
    ]
```
* or even better - include only your classes
```
tasks.withType(Test) {
    jacoco.includes = ['com.test.memusage.*']
```

Other than documenting this, I can't imagine what else we can do on the JaCoCo side.
