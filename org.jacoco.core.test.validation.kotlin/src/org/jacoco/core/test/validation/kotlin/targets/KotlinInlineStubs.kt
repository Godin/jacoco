package org.jacoco.core.test.validation.kotlin.targets

inline fun inlined() {
    println("hello") // assertFullyCovered()
}
