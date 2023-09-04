package org.jacoco.core.test.validation.kotlin.targets

object KotlinSynchronizedTarget {

    fun xxx(arg: Any?): Boolean {
        synchronized(Any()) {
        }

        synchronized(Any()) {
            return arg == null
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
    }

}
