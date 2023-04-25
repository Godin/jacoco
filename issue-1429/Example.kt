package example

public object Multik {
}

class D4Array<T>

public inline fun <reified T : Number> Multik.linspace(start: Int, stop: Int, num: Int = 50): D4Array<T> {
    return linspace(start.toDouble(), stop.toDouble(), num)
}

public inline fun <reified T : Number> Multik.linspace(start: Double, stop: Double, num: Int = 50): D4Array<T> {
    val div = num - 1.0
    val delta = stop - start
    var ret: Double = 0.0 // arange<Double>(0, stop = num)
    if (num > 1) {
        val step = delta / div
        ret *= step
    }

    ret += start
    return D4Array<T>()
}

fun main() {
  var v: D4Array<Number> = Multik.linspace(1, 1, 1)
}
