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

import static org.jacoco.core.test.validation.targets.Stubs.ex
import static org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * Test target for synchronized statement.
 */
class GroovySynchronizedTarget {

    private static final Object lock = new Object();

    private static void normal() {
        synchronized (lock) { // assertFullyCovered()
            nop() // assertFullyCovered()
        } // assertFullyCovered()
    }

    private static void explicitException() {
        synchronized (lock) { // assertFullyCovered()
            ex() // assertNotCovered()
        } // assertNotCovered()
    }

    private static void implicitException() {
        synchronized (lock) { // assertFullyCovered()
            ex() // assertNotCovered()
        } // assertNotCovered()
    }

    static void main(String[] args) {
        normal()

        try {
            explicitException()
        } catch (Exception ignore) {
        }

        try {
            implicitException()
        } catch (Exception ignore) {
        }
    }

}
