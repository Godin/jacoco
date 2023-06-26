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

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for `inline class`.
 */
object KotlinInlineClassTarget {

    interface Base {
        fun base(p: String)
    }

    @JvmInline
    value class Example( // assertEmpty()
        val value: Int // assertEmpty()
    ) : Base { // assertEmpty()
        init {
            nop() // assertFullyCovered()
        }

        val property: Int
            get() = value // assertFullyCovered()

        fun getValue() = value // assertFullyCovered()

        fun function() { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertFullyCovered()

        override fun base(p: String) { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val v = Example(42)
        v.value
        v.property
        v.function()
        v.getValue()
        v.base("")
    }

}
