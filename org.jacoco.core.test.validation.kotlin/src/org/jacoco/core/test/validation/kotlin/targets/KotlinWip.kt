/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
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
 *
 */
object KotlinWipTarget {

    private fun explicitException() {
        throw StubException() // assertFullyCovered()
    }

    private fun noExceptionTryCatch() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
        } catch (e: StubException) { // assertNotCovered()
            nop() // assertNotCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    private fun implicitExceptionTryCatch() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
            ex() // assertNotCovered()
            nop() // assertNotCovered()
        } catch (e: StubException) { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    private fun noExceptionFinally() {
        nop() // assertFullyCovered()
        try { // assertFullyCovered()
            nop() // assertFullyCovered()
        } finally { // assertEmpty()
            nop() // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    private fun example(obj: Any): Int? {
        if (obj is String) { // assertFullyCovered(0, 2)
            return obj.length;
        }
        return null
    }

    @JvmStatic
    fun main(args: Array<String>) {
        exec { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertFullyCovered()

        noexec { // assertFullyCovered()
            nop() // assertNotCovered()
        } // assertNotCovered()

        noExceptionFinally()

        try {
            explicitException()
        } catch (e: StubException) {
        }
        noExceptionTryCatch()
        implicitExceptionTryCatch()

        example(Any())
        example("")
    }

}
