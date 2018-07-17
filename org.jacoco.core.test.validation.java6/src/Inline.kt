fun t() {
    inlineFunction(true, "Hello", ::println)
}

fun f() {
    inlineFunction(false, "Hello", ::println)
}

fun b(i: Int, b: Boolean) {
    inlineFunction(b, "Hello", ::println)
}

fun main(args: Array<String>) {
}
