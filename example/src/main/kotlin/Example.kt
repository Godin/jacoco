inline fun inlineIntoExample() {
  println("inline into example")
}

fun example() {
  inlineIntoExample()
}

inline fun inlineOnlyIntoTest() {
  println("inline into test");
}
