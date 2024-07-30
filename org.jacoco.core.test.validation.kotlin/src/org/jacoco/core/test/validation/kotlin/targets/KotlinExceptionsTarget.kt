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

import org.jacoco.core.test.validation.targets.Stubs.*

/**
 * TODO add comment
 */
object KotlinExceptionsTarget {

    private fun implicitArrayIndexOutOfBoundsException(a: Array<String>) {
        nop() // assertNotCovered()
        a[0] // assertNotCovered()
        nop() // assertNotCovered()
    }

    private fun implicitException() {
        nop() // assertFullyCovered()
        ex() // assertNotCovered()
        nop() // assertNotCovered()
    }

    private fun explicitException() {
        nop() // assertFullyCovered()
        throw StubException() // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            implicitArrayIndexOutOfBoundsException(arrayOf())
        } catch (_: ArrayIndexOutOfBoundsException) {
        }
        try {
            implicitException()
        } catch (_: StubException) {
        }
        try {
            explicitException()
        } catch (_: StubException) {
        }
    }

}
