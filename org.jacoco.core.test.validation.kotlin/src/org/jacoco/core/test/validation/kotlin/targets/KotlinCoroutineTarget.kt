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
import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for coroutines.
 */
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

// a1b04a723f2d4fdda472a5697b6d64a57d5929f5
object KotlinCoroutineTarget {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        flowOf("test").filterIsInstance<String>()
    }
}
