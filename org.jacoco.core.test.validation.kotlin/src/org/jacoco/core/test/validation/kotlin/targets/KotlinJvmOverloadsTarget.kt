/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * This test target is `???`.
 */
object KotlinJvmOverloadsTarget {

    /**
     * we filter out synthetic with line number 25
     */
    @JvmOverloads
    fun foo(p: Any = "") { // assertEmpty()
        nop(p) // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        foo("")
    }
}
