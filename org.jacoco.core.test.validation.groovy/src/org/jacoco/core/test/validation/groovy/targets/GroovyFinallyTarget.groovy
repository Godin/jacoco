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
package org.jacoco.core.test.validation.groovy.targets

import org.jacoco.core.test.validation.targets.Stubs

/**
 * TODO
 */
class GroovyFinallyTarget {

    static void example() {
        Stubs.nop("before") // assertFullyCovered()

        try {
            Stubs.ex(false) // assertFullyCovered()
        } finally { // assertFullyCovered()
            Stubs.nop("finally") // assertFullyCovered()
        } // assertEmpty()

        Stubs.nop("after") // assertFullyCovered()
    }

    static void emptyCatch() {
        Stubs.nop("before")

        try { // assertEmpty()
            Stubs.nop("try.1") // assertFullyCovered()
            Stubs.nop("try.2") // assertFullyCovered()
        } catch (Exception e) { // assertPartlyCovered()
            /* empty */
        } finally { // assertNotCovered()
            Stubs.nop("finally.1") // assertFullyCovered()
            Stubs.nop("finally.2") // assertFullyCovered()
        } // assertFullyCovered()

    }

    static void emptyCatch_2() {
        Stubs.nop("before")

        try { // assertEmpty()
            Stubs.nop("try.1") // assertFullyCovered()
            Stubs.nop("try.2") // assertFullyCovered()
        } catch (Exception e) { // assertPartlyCovered()
            /* empty */
        } finally { // assertNotCovered()
            Stubs.nop("finally.1") // assertFullyCovered()
        } // assertEmpty()
    }

    static void main(String[] args) {
        example()

        emptyCatch()
        emptyCatch_2()
    }

}
