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

/**
 * TODO https://github.com/JetBrains/kotlin/blob/v2.1.10/plugins/kotlinx-serialization/kotlinx-serialization.backend/src/org/jetbrains/kotlinx/serialization/compiler/backend/ir/IrPreGenerator.kt#L59
 */
object KotlinWipTarget {

    @kotlinx.serialization.Serializable // assertEmpty()
    data class Example( // assertFullyCovered()
        val data: String
    )

    @JvmStatic
    fun main(args: Array<String>) {
        Example("")
    }

}
