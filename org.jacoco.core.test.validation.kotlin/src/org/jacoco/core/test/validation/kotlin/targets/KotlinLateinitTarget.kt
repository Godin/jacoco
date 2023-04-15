/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
 * This test target is `lateinit` property.
 */
object KotlinLateinitTarget {
    class Example<T : Any> {
        lateinit var publicMember: String // assertFullyCovered()
        lateinit var publicGenericMember: T // assertFullyCovered()
        private lateinit var privateMember: String // assertEmpty()
        private lateinit var privateGenericMember: T // assertEmpty()
        fun getPrivateMember() = privateMember // assertFullyCovered()
        fun getPrivateGenericMember() = privateGenericMember // assertFullyCovered()

        fun init(value: T) {
            publicMember = ""
            publicGenericMember = value
            privateMember = ""
            privateGenericMember = value
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val e = Example<String>()
        e.init("")

        e.publicMember // assertFullyCovered()
        e.publicGenericMember // assertFullyCovered()
        e.getPrivateMember() // assertFullyCovered()
        e.getPrivateGenericMember() // assertFullyCovered()
    }
}
