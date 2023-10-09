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

/**
 * TODO
 * this generates class with name containing "$$inlined"
 * https://github.com/JetBrains/kotlin/blob/afdd8466cc4c72c441e9ab039fde168de7fa8a34/compiler/backend/src/org/jetbrains/kotlin/codegen/inline/inlineCodegenUtils.kt#L61
 */
object KotlinCrossinlineTarget {

    inline fun test(crossinline lambda: () -> Unit): () -> Unit {
        println("body of inline function") // assertNotCovered()
        return {
            println("constructed") // assertFullyCovered()
            lambda() // assertFullyCovered()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        test {
            println("inside") // assertEmpty()
        }()
    }

}
