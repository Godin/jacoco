/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import static org.jacoco.core.test.validation.targets.Stubs.f
import static org.jacoco.core.test.validation.targets.Stubs.i1
import static org.jacoco.core.test.validation.targets.Stubs.i2
import static org.jacoco.core.test.validation.targets.Stubs.i3
import static org.jacoco.core.test.validation.targets.Stubs.nop
import static org.jacoco.core.test.validation.targets.Stubs.t

/**
 * Test target for Groovy control structures.
 */
class GroovyControlStructuresTarget {

    private static void missedIfBlock() {

        if (f()) { // assertFullyCovered(1, 1)
            nop() // assertNotCovered()
        } else {
            nop() // assertFullyCovered()
        }

    }

    private static void executedIfBlock() {

        if (t()) { // assertFullyCovered(1, 1)
            nop() // assertFullyCovered()
        } else {
            nop() // assertNotCovered()
        }

    }

    private static void missedWhileBlock() {

        while (f()) { // assertFullyCovered(1, 1)
            nop() // assertNotCovered()
        }

    }

    private static void executedWhileBlock() {

        def i = 0
        while (i++ < 3) { // assertFullyCovered(0, 2)
            nop() // assertFullyCovered()
        }

    }

    private static void missedForBlock() {

        for (nop(); f(); nop()) { // assertPartlyCovered(1, 1)
            nop() // assertNotCovered()
        }

    }

    private static void executedForBlock() {

        for (def j = 0; j < 1; j++) { // assertFullyCovered(0, 2)
            nop() // assertFullyCovered()
        }

    }

    private static void missedForEachBlock() {

        for (Object o : Collections.emptyList()) { // assertPartlyCovered(1, 1)
            nop(o) // assertNotCovered()
        }

    }

    private static void executedForEachBlock() {

        for (Object o : Collections.singleton(new Object())) { // assertFullyCovered(0,2)
            nop(o) // assertFullyCovered()
        }

    }

    private static void switchStatement() {

        switch (i2()) { // assertFullyCovered()
            case 1: // assertFullyCovered(1, 1)
                nop() // assertNotCovered()
                break
            /* TODO */
            case 2: // assertPartlyCovered(1, 1)
                nop() // assertFullyCovered()
                break
            default: // assertEmpty()
                nop() // assertNotCovered()
                break
        }

    }

    private static void breakStatement() {

        while (true) {
            if (t()) {
                break // assertFullyCovered()
            }
            nop() // assertNotCovered()
        }

    }

    private static void continueStatement() {

        for (def j = 0; j < 1; j++) {
            if (t()) {
                continue // assertFullyCovered()
            }
            nop() // assertNotCovered()
        }

    }

    private static void implicitReturn() {
    } // assertFullyCovered()

    private static void explicitReturn() {

        return // assertFullyCovered()

        /* TODO */
    } // assertNotCovered()

    static void main(String[] args) {
        missedIfBlock()
        executedIfBlock()
        missedWhileBlock()
        executedWhileBlock()
        missedForBlock()
        executedForBlock()
        missedForEachBlock()
        executedForEachBlock()
        switchStatement()
        breakStatement()
        continueStatement()
        implicitReturn()
        explicitReturn()
    }

}
