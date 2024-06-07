/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

/**
 * https://youtrack.jetbrains.com/issue/KT-14663
 * https://github.com/Kotlin/KEEP/blob/explicit-backing-fields-re/proposals/explicit-backing-fields.md
 * https://github.com/Kotlin/KEEP/issues/278
 */
object KotlinExplicitBackingFieldsTarget {

    class C {
        val elementList: List<String> // assertNotCovered()
            field = kotlin.collections.mutableListOf() // assertFullyCovered()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        C()
    }

}
