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

/**
 * TODO
 */
object PrivateEmptyNoArgConstructorTarget {

    class Example private constructor() { // assertEmpty()
        constructor(s: String) : this() // assertFullyCovered()
    }

    /* non-empty TODO validation test does not analyze this class even if it was instrumented */
    class E2 private constructor() { // assertNotCovered()
        init {
            Stubs.nop() // assertNotCovered()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Stubs.nop(E2::class.java.declaredMethods)
        Example("")
    }

}
