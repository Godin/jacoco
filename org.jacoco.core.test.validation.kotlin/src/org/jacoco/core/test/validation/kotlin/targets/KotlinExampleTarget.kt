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
 * TODO rename "KotlinWhenStringTarget", add cases with explicit default
 */
object KotlinExampleTarget {

    private fun explicitDefaultNotCovered(s: String) {
        when (s) { // assertNotCovered(4, 0)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
            else -> nop("else") // assertNotCovered()
        } // assertEmpty()
    } // assertNotCovered()

    private fun explicitDefaultCovered(s: String) {
        when (s) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
            else -> nop("else") // assertFullyCovered()
        } // assertEmpty()
    }

    /**
     * "a" passed
     * TODO should it be consistent with [wipInt] and Java ?
     */
    private fun wip(s: String) {
        when (s) { // assertFullyCovered(3, 1)
            "a", "b" -> nop(" case a,b") // assertFullyCovered()
            "c" -> nop("case c") // assertNotCovered()
            else -> nop("else") // assertNotCovered()
        }
    }

    /** 1 passed **/
    private fun wipInt(i: Int) {
        when (i) { // assertFullyCovered(2, 1)
            1, 2 -> nop("case 1, 2") // assertFullyCovered()
            3 -> nop("case 3") // assertNotCovered()
            else -> nop("else") // assertNotCovered()
        }
    }

    /** empty string passed */
    private fun nullableImplicitDefault(s: String?) {
        when (s) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
        } // assertEmpty()
    }

    /** null passed */
    private fun nullableImplicitDefault2(s: String?) {
        when (s) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
        } // assertEmpty()
    }

    /** empty string passed */
    private fun nullableExplicitDefault(s: String?) {
        when (s) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
            else -> nop("else") // assertFullyCovered()
        } // assertEmpty()
    }

    /** null passed **/
    private fun nullableExplicitDefault2(s: String?) {
        when (s) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
            else -> nop("else") // assertFullyCovered()
        } // assertEmpty()
    }

    /** null passed **/
    private fun nullableCase(s: String?) {
        when (s) { // assertFullyCovered(4, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
            null -> nop("null") // assertFullyCovered()
            else -> nop("else") // assertNotCovered()
        }
    }

    /**
     * TODO rename "case covered"? cover all cases except default?
     */
    private fun implicitDefaultNotCovered(s: String) {
        when (s) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertFullyCovered()
            "c" -> nop("case c") // assertNotCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    private fun implicitDefaultCovered(s: String) {
        when (s) { // assertFullyCovered(3, 1)
            "a" -> nop("case a") // assertNotCovered()
            "b" -> nop("case b") // assertNotCovered()
            "c" -> nop("case c") // assertNotCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    private fun fullyCovered(s: String) {
        when (s) { // assertFullyCovered(0, 4)
            "a" -> nop("case a") // assertFullyCovered()
            "b" -> nop("case b") // assertFullyCovered()
            "c" -> nop("case c") // assertFullyCovered()
        } // assertEmpty()
    } // assertFullyCovered()

    /**
     * TODO rename, add comment
     */
    private fun no(s: String) {
        when (s) { // assertFullyCovered()
            "a" -> nop("case a") // assertFullyCovered(0, 2)
            "b" -> nop("case b") // assertFullyCovered(0, 2)
        } // assertEmpty()
    } // assertFullyCovered()

    private fun whenInt(i: Int) {
        when (i) { // assertFullyCovered(3, 1)
            1 -> nop("1")
            2 -> nop("2")
            3 -> nop("3")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        wipInt(1)

        wip("a")

        explicitDefaultCovered("")

        nullableImplicitDefault("")

        nullableImplicitDefault2(null)

        nullableExplicitDefault("")

        nullableExplicitDefault2(null)

        nullableCase(null)

        implicitDefaultNotCovered("b")

        implicitDefaultCovered("")

        fullyCovered("a")
        fullyCovered("b")
        fullyCovered("c")
        fullyCovered("")

        no("a")
        no("b")
        no("")

        whenInt(2)
    }

}
