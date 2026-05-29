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

/**
 * Test target containing `when` with subject of type `sealed class`
 * and `if` that produce indistinguishable bytecode.
 *
 * Even `LINENUMBER`s do not help to disambiguate all cases.
 * They are also different between compiler versions - see for example
 * [KT-74655](https://youtrack.jetbrains.com/issue/KT-74655/K2-Synthetic-branch-for-when-has-LINENUMBER-in-LNT).
 */
object KotlinWhenSealedIndistinguishableFromIfTarget {

    private fun nonSealedIf(p: Any) =
        if (p is String) "if" else throw NoWhenBranchMatchedException() // assertFullyCovered()

    /* @formatter:off */
    private fun sealedWhen(p: S1) =
        when (p) { is S1.A -> "S1.A" } // assertFullyCovered()
    /* @formatter:on */

    private fun sealedWhenFormatted(p: S1) =
        when (p) { // assertFullyCovered()
            is S1.A -> "S1.A" // assertFullyCovered()
        } // assertFullyCovered()

    private fun sealedWhenFormatted2(p: S2) =
        when (p) { // assertFullyCovered()
            is S2.A -> "S2.A" // assertFullyCovered(0, 2)
            is S2.B -> "S2.B" // assertFullyCovered()
        } // assertFullyCovered()

    private sealed class S1 {
        object A : S1()
    }

    private sealed class S2 {
        object A : S2()
        object B : S2()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        nonSealedIf("")
        sealedWhen(S1.A)
        sealedWhenFormatted(S1.A)
        sealedWhenFormatted2(S2.A)
        sealedWhenFormatted2(S2.B)
    }

}
