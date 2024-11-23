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
 * Test target with `when` expressions and statements with subject of type `enum class`.
 */
object KotlinWhenEnumTarget {

    private enum class Enum {
        A, B
    }

    private fun expression(enum: Enum): String =
        when (enum) {  // assertFullyCovered(0, 2)
            Enum.A -> "a" // assertFullyCovered()
            Enum.B -> "b" // assertFullyCovered()
        } // assertFullyCovered()

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun whenEnumRedundantElse(enum: Enum): String =
        when (enum) { // assertFullyCovered(0, 2)
            Enum.A -> "a" // assertFullyCovered()
            Enum.B -> "b" // assertFullyCovered()
            else -> throw NoWhenBranchMatchedException() // assertEmpty()
        } // assertFullyCovered()

    private fun whenByNullableEnumWithNullCaseAndWithoutElse(e: Enum?): String =
        when (e) { // assertFullyCovered(0, 3)
            Enum.A -> "a" // assertFullyCovered()
            Enum.B -> "b" // assertFullyCovered()
            null -> "null" // assertFullyCovered()
        } // assertFullyCovered()

    private fun whenByNullableEnumWithoutNullCaseAndWithElse(e: Enum?): String =
        when (e) { // assertFullyCovered(0, 3)
            Enum.A -> "a" // assertFullyCovered()
            Enum.B -> "b" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
        } // assertFullyCovered()

    private fun whenByNullableEnumWithNullAndElseCases(e: Enum?): String =
        when (e) { // assertFullyCovered(0, 3)
            Enum.A -> "a" // assertFullyCovered()
            null -> "null" // assertFullyCovered()
            else -> "else" // assertFullyCovered()
        } // assertFullyCovered()

    /**
     * Since Kotlin 1.7 `when` statement with subject of type `enum class`
     * must be exhaustive (error otherwise, warning in 1.6) however
     * Kotlin compiler prior to version 2.0 was generating bytecode
     * indistinguishable from [wip] that TODO
     *
     * TODO investigate differences between Kotlin compiler versions 1.9.23 and 2.0.0
     * TODO add examples of nullable?
     */
    private fun statement(enum: Enum) { // assertEmpty()
        when (enum) { // assertFullyCovered(0, 2)
            Enum.A -> nop("a") // assertFullyCovered()
            Enum.B -> nop("b") // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    enum class E2 {
        A, B, C
    }

    private fun wip(e: E2) { // assertEmpty()
        when (e) { // assertFullyCovered(0, 3)
            E2.A -> nop("a") // assertFullyCovered()
            E2.B -> nop("b") // assertFullyCovered()
            else -> Unit // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        expression(Enum.A)
        expression(Enum.B)

        whenEnumRedundantElse(Enum.A)
        whenEnumRedundantElse(Enum.B)

        whenByNullableEnumWithNullCaseAndWithoutElse(Enum.A)
        whenByNullableEnumWithNullCaseAndWithoutElse(Enum.B)
        whenByNullableEnumWithNullCaseAndWithoutElse(null)

        whenByNullableEnumWithoutNullCaseAndWithElse(Enum.A)
        whenByNullableEnumWithoutNullCaseAndWithElse(Enum.B)
        whenByNullableEnumWithoutNullCaseAndWithElse(null)

        whenByNullableEnumWithNullAndElseCases(Enum.A)
        whenByNullableEnumWithNullAndElseCases(Enum.B)
        whenByNullableEnumWithNullAndElseCases(null)

        statement(Enum.A)
        statement(Enum.B)

        wip(E2.A)
        wip(E2.B)
        wip(E2.C)
    }

}
