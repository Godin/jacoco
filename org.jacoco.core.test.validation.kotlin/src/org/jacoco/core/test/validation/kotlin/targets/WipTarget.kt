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

import org.jacoco.core.test.validation.targets.Stubs

/**
 * TODO
 *
 * https://github.com/zcash/zcash-android-wallet-sdk/blob/afba5b74a639a8ddeece28259d49b0993cc5d318/sdk-lib/src/main/java/cash/z/ecc/android/sdk/internal/Twig.kt#L195-L204
 */
object WipTarget {

    inline fun example(priority: Int = 0): Int {
        val x = 0.0
        val y = 0.0
        Stubs.nop(priority + x + y)
        return 0
    }

    @JvmStatic
    fun main(args: Array<String>) {
    }

}
