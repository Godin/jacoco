/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs

/**
 * TODO
 */
object KotlinEnumClassTarget {

    enum class E { // assertEmpty()
        CONST(if (Stubs.f()) Any() else Any()); // assertPartlyCovered(1, 1)

        constructor(o: Any) {}

        init {
            Stubs.nop() // assertFullyCovered()
        }

        /**
         * This method should not be excluded from analysis unlike implicitly
         * created [values] method.
         */
        fun values(o: Any?) {} // assertNotCovered()

        /**
         * This method should not be excluded from analysis unlike implicitly
         * created [valueOf] method.
         */
        fun valueOf() {} // assertNotCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        E.CONST
    }

}
