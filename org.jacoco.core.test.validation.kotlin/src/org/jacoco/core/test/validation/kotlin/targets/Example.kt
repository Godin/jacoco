package org.jacoco.core.test.validation.kotlin.targets

@JvmInline
value class Example(val value: String) : BaseJava {
    override fun baseJava(example: Example?) {
    }
}

fun main() {
    val example: BaseJava = Example("")
    example.baseJava(null)
}
