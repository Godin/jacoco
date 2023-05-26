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

import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * TODO
 * Could
 * https://github.com/jacoco/jacoco/issues/1410
 * be related to
 * https://github.com/jacoco/jacoco/pull/1019
 * ?
 */
object KotlinWipTarget {

    /** pop from let ( @kotlin.internal.InlineOnly ) is not ignored */

    suspend fun <T> List<T>.z() : T = this[0]

    suspend inline fun <reified T : Any> List<T>.y() : T =
        when (T::class) {
            Unit::class -> this.z().let { Unit as T }
            else -> this[0]
        }

    suspend fun x(): String = Collections.singletonList("").y() // assertFullyCovered()

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            x()
        }
    }

}
