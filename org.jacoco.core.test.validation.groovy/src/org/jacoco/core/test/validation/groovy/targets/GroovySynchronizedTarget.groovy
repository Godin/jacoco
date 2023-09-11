package org.jacoco.core.test.validation.groovy.targets

import org.jacoco.core.test.validation.targets.Stubs

class GroovySynchronizedTarget {

    // TODO NOK due to probe inserted for protected RETURN
    static boolean nok(Object lock) {
        synchronized (lock) {
            Stubs.nop()
            return false
        }
    }

    static boolean ok(Object lock) {
        synchronized (lock) {
            Stubs.nop()
        }
        return false
    }

    static void main(String[] args) {
        nok(args)
        ok(args)
    }

}
