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
 * TODO find better name
 */
class GroovyWipTarget {

    /**
     * TODO unfortunate side effect
     */
    private static boolean non_throwing_try(boolean b) {
        boolean x
        try {
            return x = !b // assertPartlyCovered(2, 0)
        } finally {
            Stubs.nop()
        }
    }

    /**
     * TODO unfortunate side effect
     */
    private static boolean non_throwing_synchronized(boolean b) {
        boolean x
        synchronized (new Object()) {
            x = !b // assertPartlyCovered(2, 0)
        }
        return x
    }

    private static boolean throwing_synchronized(boolean b) {
        boolean x
        synchronized (new Object()) {
            Stubs.nop()
            x = !b // assertFullyCovered(0, 2)
        }
        return x
    }

    /**
     * TODO
     * after MONITOREXIT there are instructions protected by catch-any handler
     * IDEA: special-case RETURN
     */
    static boolean nok(Object lock) { // assertEmpty()
        synchronized (lock) { // assertFullyCovered()
            Stubs.nop() // assertNotCovered()
            return false // assertNotCovered()
        } // assertNotCovered()
    } // assertEmpty()

    static boolean ok(Object lock) { // assertEmpty()
        synchronized (lock) { // assertFullyCovered()
            Stubs.nop() // assertFullyCovered()
        } // assertFullyCovered()
        return false // assertFullyCovered()
    } // assertEmpty()

    static void main(String[] args) {
        non_throwing_try(true)
        non_throwing_try(false)

        non_throwing_synchronized(true)
        non_throwing_synchronized(false)

        throwing_synchronized(true)
        throwing_synchronized(false)

        nok(args)
        ok(args)
    }

}
