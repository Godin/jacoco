val a = true
val b = true

inline fun condA(): Boolean {
  return a
}

inline fun condB(): Boolean {
  return b
}

fun example() {
  if (condA() && condB()) {
    println()
  }
}
