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

import static org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * TODO
 */
class GroovyTraitTarget {

    trait T {
        void foo() {
            nop("foo") // assertEmpty()
        }
        void bar() {
            nop("bar") // assertEmpty()
        }
        void baz() {
            nop("baz") // assertEmpty()
        }
    }

    static class C implements T { // assertEmpty()
        @Override
        void foo() { // assertEmpty()
        } // assertFullyCovered()
    }

    static void main(String[] args) {
        def c = new C()
        c.foo()
        c.bar()
    }

}
