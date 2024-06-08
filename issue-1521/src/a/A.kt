package a;

@Suppress("UNUSED_PARAMETER")
public fun a0(p: suspend () -> Unit) {
}

public inline fun a(crossinline p: suspend () -> Unit) = a0 { p() }
