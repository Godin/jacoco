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

    private data class A(val b: B)
    private data class B(val c: String)

    private fun example(a: A?) {
        nop(a?.b?.c) // assertFullyCovered(0, 4)
    }

    private fun example2(a: A?) {
        nop(a?.b?.c ?: "") // assertFullyCovered(0, 6)
    }

    private fun example3(a1: A?, a2: A?) {
        nop(a1?.b?.c ?: a2?.b?.c ?: "")
    }

    private fun example4(a1: A?, a2: A?, a3: A?) {
        nop(a1?.b?.c ?: a2?.b?.c ?: a3?.b?.c ?: "")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        example(A(B("")))
        example(null)

        example2(A(B("")))
        example2(null)
    }

}
