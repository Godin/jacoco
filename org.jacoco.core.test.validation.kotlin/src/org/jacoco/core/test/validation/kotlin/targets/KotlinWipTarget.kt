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

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * See
 * https://youtrack.jetbrains.com/issue/IDEA-258371
 * https://youtrack.jetbrains.com/issue/KT-48311/Incorrect-LINENUMBER-after-if-with-a-suspend-call
 */
object KotlinWipTarget {

    private suspend fun example(b: Boolean) {
        if (b) { // assertFullyCovered(1, 1)
            yield() // assertNotCovered()
        } // assertEmpty()
        nop() // assertFullyCovered()
    }

    private fun example2(b: Boolean) {
        if (b) { // assertFullyCovered(1, 1)
            nop() // assertNotCovered()
        } // assertEmpty()
        nop() // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        example(false)
        example2(false)
    }

}
