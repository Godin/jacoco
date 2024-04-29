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

    private fun example(x: String?): Int? {
        return x?.length // assertFullyCovered(0, 2)
    }

    data class Item(val s: String)
    data class Container(val item:Item)

    private fun e1(c: Container?) {
        nop(c?.item?.s); // assertFullyCovered(0, 2)
    }

    private fun e2(c: Container?) {
        /* FIXME? */
        nop(c?.item?.s ?: "") // assertFullyCovered(0, 4)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        example("")
        example(null)

        e1(null)
        e1(Container(Item("")))

        e2(null)
        e2(Container(Item("")))
    }

}
