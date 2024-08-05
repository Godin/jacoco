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

import org.jacoco.core.test.validation.targets.Stubs
import java.lang.Exception

/**
 * TODO
 */
object KotlinFinallyTarget {

    private fun tryFinally(t: Boolean) {
        try { // assertFullyCovered()
            Stubs.ex(t); // assertFullyCovered()
        } finally { // assertEmpty()
            Stubs.nop(if (t) 1 else 2); // assertFullyCovered(0, 2)
        } // assertEmpty()
    }

    @JvmStatic
    fun main(args: Array<String>) {

        tryFinally(false);
        try {
            tryFinally(true);
        } catch (_: Exception) {
        }

        try { // assertFullyCovered()
            Stubs.nop() // assertFullyCovered()
        } catch (e: Exception) { // assertNotCovered()
            /* empty */
        } finally { // assertEmpty()
            Stubs.nop() // assertFullyCovered()
        } // assertEmpty()

    }

}
