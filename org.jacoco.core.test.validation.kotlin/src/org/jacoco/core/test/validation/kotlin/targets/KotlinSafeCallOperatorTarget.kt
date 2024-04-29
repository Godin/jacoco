/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for safe call operator.
 */
object KotlinSafeCallOperatorTarget {

    data class Item(val s: String)
    data class Container(val i: Item)

    private fun example(c: Container?) {
        nop(c?.i?.s) // assertFullyCovered(0, 4)
    }

    private fun example2(c: Container?) {
        nop(c?.i?.s ?: "") // assertFullyCovered(0, 6)
    }

    private fun example3(a: Container?, b: Container?) {
        nop(a?.i?.s ?: b?.i?.s ?: "")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        example(Container(Item("")))
        example(null)

        example2(Container(Item("")))
        example2(null)
    }

}
