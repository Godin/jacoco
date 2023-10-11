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
object KotlinHardInlineTarget {

    private inline fun example(i: Int) {
        if (i > 13)
            /* coverage in IntelliJ marks line as fully covered */
            noexec { nop("true") }
        else
            nop("false")
    }

    @JvmStatic
    fun main(args: Array<String>) {

        example(42)

    }

}
