/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.test.validation.kotlin.targets.wip.trigger

/**
 * https://youtrack.jetbrains.com/issue/KT-74617/Trivial-SMAP-optimization-leads-to-missing-debug-info-after-inline
 */
inline fun <reified T> example(): T? {
    nop {
        "" is T // assertEmpty()
    }
    return null
}

fun nop(x: Runnable) = x.run()

fun main(args: Array<String>) {
    trigger()
}
