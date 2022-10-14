package org.example

import kotlinx.coroutines.debug.junit4.CoroutinesTimeout
import net.bytebuddy.ByteBuddy
import net.bytebuddy.agent.ByteBuddyAgent
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy
import org.junit.Rule
import org.junit.Test

class ExampleTest {
    @get:Rule
    val timeout = CoroutinesTimeout.seconds(100)

    @Test
    fun bytebuddy() {
        // https://github.com/raphw/byte-buddy/issues/1248
        ByteBuddyAgent.install()
        ByteBuddy()
            .redefine(ExampleTest::class.java, ClassFileLocator.ForInstrumentation.fromInstalledAgent(ExampleTest::class.java.classLoader))
            .make()
            .load(ExampleTest::class.java.classLoader, ClassReloadingStrategy.fromInstalledAgent())
    }

    @Test
    fun testSomething() {
    }

//    @Test
//    fun testThatHangs() = runBlocking {
////        delay(Long.MAX_VALUE) // somewhere deep in the stack
//    }
}
