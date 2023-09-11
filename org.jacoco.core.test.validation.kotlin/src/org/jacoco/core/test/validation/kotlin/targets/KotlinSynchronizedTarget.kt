package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs

object KotlinSynchronizedTarget {

    @JvmStatic
    fun main(args: Array<String>) {
        synchronized(Any()) {
            if (Stubs.f()) {
                Stubs.nop()
            }
        }
    }

}
