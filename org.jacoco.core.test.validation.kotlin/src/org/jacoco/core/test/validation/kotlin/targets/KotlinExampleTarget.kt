package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs

object KotlinExampleTarget {

    private inline fun branch(b: Boolean) {
        if (b) println("a") else println("b")
    }

    private fun branchOptimizedCallsite() {
        branch(true)
//        example(false)
    }

    private fun branchUnoptimizedCallsite(b: Boolean) {
        branch(b)
    }

    private inline fun lambda() {
        Stubs.noexec { println() }
        println()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        branchOptimizedCallsite()
        branchUnoptimizedCallsite(true)
        lambda()
    }

}
