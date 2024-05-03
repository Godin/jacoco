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
 * Test target for `inline class`.
 */
object KotlinInlineClassTarget {

    interface Base {
        fun base()

        fun d() = Unit
    }

    @JvmInline
    value class Example(val value: String) : Base { // assertEmpty()

        constructor() : this("") // assertFullyCovered()

        init { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertEmpty()

        fun function() { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertFullyCovered()

        override fun base() { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertFullyCovered()

    } // assertEmpty()

    @JvmStatic
    fun main(args: Array<String>) {
        val v = Example()
        v.value
        v.function()
        v.base()
        v.d()
    }

}
