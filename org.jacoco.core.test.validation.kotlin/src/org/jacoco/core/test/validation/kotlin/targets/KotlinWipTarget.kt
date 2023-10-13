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

import org.jacoco.core.test.validation.targets.Stubs.noexec
import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * TODO
 */
@Suppress("NOTHING_TO_INLINE")
object KotlinWipTarget {

    private var x: Boolean = true

    private inline fun bothBranches() { // assertEmpty()
        if (x) // assertFullyCovered()
            nop("true") // assertFullyCovered()
        else // assertEmpty()
            nop("false") // assertFullyCovered()
    } // assertFullyCovered()

    private inline fun onlyOneBranch() { // assertEmpty()
        if (x) // assertFullyCovered()
            nop() // assertFullyCovered()
        else // assertEmpty()
            nop() // assertNotCovered()
    } // assertFullyCovered()

    private inline fun onlyOneBranchSingleLine() {
        if (x) nop() else nop() // assertFullyCovered()
    } // assertFullyCovered()

    private inline fun wip() {
        /* FIXME status of next line is unstable - depends on order of analysis of class files */
        noexec { nop() } // assertPartlyCovered()
    }

    private inline fun level0() {
        nop() // assertFullyCovered()
    }

    private inline fun level1() {
        nop() // assertFullyCovered()
        level0() // assertFullyCovered()
    }

    @Suppress("unused")
    private inline fun unused() {
        nop() // assertNotCovered()
    } // assertNotCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        onlyOneBranch()
        onlyOneBranchSingleLine()
        bothBranches() // assertFullyCovered()
        x = false
        bothBranches()

        wip()

        level1()
    }

}
