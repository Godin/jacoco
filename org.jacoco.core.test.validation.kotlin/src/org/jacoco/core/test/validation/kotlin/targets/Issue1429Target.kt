/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

class D4Array<T>

public inline fun <reified T : Number> Issue1429Target.linspace(start: Int, stop: Int, num: Int = 50): D4Array<T> {
    return Issue1429Target.linspace(start.toDouble(), stop.toDouble(), num)
}

public inline fun <reified T : Number> Issue1429Target.linspace(start: Double, stop: Double, num: Int = 50): D4Array<T> {
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

/**
 * TODO
 */
object Issue1429Target {
}

fun main() {
    var v: D4Array<Number> = Issue1429Target.linspace(1, 1, 1)
}
