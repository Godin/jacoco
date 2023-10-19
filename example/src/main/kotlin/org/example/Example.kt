package org.example;

inline fun inlined() {
}

fun callsite() {
  inlined()
}

inline fun inlined2() {
}
