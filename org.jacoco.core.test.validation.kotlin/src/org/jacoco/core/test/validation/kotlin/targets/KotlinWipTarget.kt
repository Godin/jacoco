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

import org.jacoco.core.test.validation.targets.Stubs

/**
 * TODO find better name
 */
object KotlinWipTarget {

    /**
     * TODO unfortunate side effect
     */
    private fun non_throwing_try(b: Boolean): Boolean {
        try {
            return !b // assertPartlyCovered(2, 0)
        } finally {
            Stubs.nop() // assertFullyCovered()
        }
    }

    /**
     * TODO unfortunate side effect
     */
    private fun non_throwing_synchronized(b: Boolean): Boolean {
        synchronized(Any()) {
            return !b // assertPartlyCovered(2, 0)
        }
    }

    private fun throwing_synchronized(b: Boolean): Boolean {
        synchronized(Any()) {
            Stubs.nop() // assertFullyCovered()
            return !b // assertFullyCovered(0, 2)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        non_throwing_try(true)
        non_throwing_try(false)

        non_throwing_synchronized(true)
        non_throwing_synchronized(false)

        throwing_synchronized(true)
        throwing_synchronized(false)
    }

}
