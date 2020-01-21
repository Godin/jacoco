package org.jacoco.core.test.validation.kotlin.targets

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.lang.RuntimeException

data class Node(val left: Node?, val right: Node?)

suspend fun visit(node: Node?, depth: Int) {
    if (node == null) {
        return
    }
    yield()
    visit(node.left, depth + 1)
    if (depth == 1_000_000)
        RuntimeException().printStackTrace()
    visit(node.right, depth + 1)
}

fun main() {
    var root = Node(null, null)
    for (i in 0..1_000_000) {
        root = Node(root, null)
    }

    runBlocking {
        visit(root, 0)
    }
}
