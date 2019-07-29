package org.jacoco.core.test.validation.kotlin.targets

import org.jacoco.core.test.validation.targets.Stubs
import java.lang.Exception

/**
 * TODO
 */
object KotlinFinallyTarget {

    private fun tryFinally(t: Boolean) {
        try { // assertFullyCovered()
            Stubs.ex(t); // assertFullyCovered()
        } finally { // assertEmpty()
            Stubs.nop(if (t) 1 else 2); // assertFullyCovered(0, 2)
        } // assertEmpty()
    }

    @JvmStatic
    fun main(args: Array<String>) {

        tryFinally(false);
        try {
            tryFinally(true);
        } catch (e: Exception) {
        }

        try { // assertFullyCovered()
            Stubs.nop() // assertFullyCovered()
        } catch (e: Exception) { // assertNotCovered()
            /* empty */
        } finally { // assertEmpty()
            Stubs.nop() // assertFullyCovered()
        } // assertEmpty()

    }

}
