inline fun inlineFunction(b: Boolean, str: String, expression: (String) -> Unit) {
    if (b) {
        expression(str)
    }
}
