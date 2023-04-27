package example

inline fun <R> example(priority: Int = 0, block: () -> R): R {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsed = (System.currentTimeMillis() - start)
    return result
}
