/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
            Stubs.nop("finally.1") // assertFullyCovered()
            /* TODO see execution path with exception in case of one line in debugger */
            Stubs.nop("finally.2") // assertFullyCovered()
        } // assertFullyCovered()

        Stubs.nop("after") // assertFullyCovered()
    }

    static void example_2() {
        Stubs.nop("before") // assertFullyCovered()

        try {
            Stubs.ex(false) // assertFullyCovered()
        } finally { // assertFullyCovered()
            Stubs.nop("finally.1") // assertEmpty()
        } // assertFullyCovered()

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
            Stubs.nop("finally.1") // assertEmpty()
        } // assertFullyCovered()
    }

    static void main(String[] args) {
        example()
        example_2()

        emptyCatch()
        emptyCatch_2()
    }

}
