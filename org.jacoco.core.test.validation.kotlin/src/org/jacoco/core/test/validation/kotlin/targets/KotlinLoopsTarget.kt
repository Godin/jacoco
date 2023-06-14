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

import org.jacoco.core.test.validation.targets.Stubs.i1
import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * TODO https://github.com/jacoco/jacoco/pull/1245
 */
object KotlinLoopsTarget {

    @JvmStatic
    fun main(args: Array<String>) {

        for (j in 0 until i1()) { // assertFullyCovered(0, 2)
            nop() // assertFullyCovered()
        }

        for (j in i1() downTo 0) { // assertFullyCovered(0, 2)
            nop() // assertFullyCovered()
        }

        val limit = 10
        for (j in limit downTo i1()) { // assertFullyCovered(1, 3)
            nop() // assertFullyCovered()
        }

    }

}
