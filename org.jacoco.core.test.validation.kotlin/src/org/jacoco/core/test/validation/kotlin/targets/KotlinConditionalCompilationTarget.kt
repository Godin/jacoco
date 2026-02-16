/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
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
 * Conditional compilation.
 */
object KotlinConditionalCompilationTarget {

    private const val FALSE = false
    private const val TRUE = true

    private fun conditionFalse() {
        if (FALSE) { // assertFullyCovered()
            nop("then") // assertEmpty()
        } else { // assertEmpty()
            nop("else") // assertFullyCovered()
        } // assertEmpty()
    }

    private fun conditionTrue() {
        if (TRUE) { // assertFullyCovered()
            nop("then"); // assertFullyCovered()
        } else { // assertEmpty()
            nop("else"); // assertEmpty()
        } // assertEmpty()
    }

    private fun whileLoop() {
        while (TRUE) { // assertNotCovered()
            nop() // assertNotCovered()
            break // assertNotCovered()
        } // assertEmpty()
    }

    private fun doWhileLoop() {
        do { // assertEmpty()
            nop() // assertNotCovered()
        } while (FALSE) // assertNotCovered()
    }

    private fun doDoWhileLoop2() {
        do { // assertEmpty()
            nop() // assertNotCovered()
        } while (TRUE) // assertNotCovered()
    }

    private fun doDoWhileLoop3() {
        do { // assertEmpty()
            break // assertNotCovered()
        } while (TRUE) // assertEmpty()
    }

    /**
     * cf with Java
     */
    private fun wip() {
        when (2) { // assertNotCovered()
            0 -> nop("0") // assertNotCovered()
            1 -> nop("1") // assertNotCovered()
            2 -> nop("2") // assertNotCovered()
            else -> nop("else") // assertEmpty()
        } // assertEmpty()

        when (0) { // assertNotCovered()
            0 -> nop("0") // assertNotCovered()
            1 -> nop("1") // assertEmpty()
            2 -> nop("2") // assertEmpty()
            else -> nop("else") // assertEmpty()
        } // assertEmpty()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        conditionFalse()
        conditionTrue()
    }

}
