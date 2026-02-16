/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * <a href=
 * "https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#d5e27651">
 * Conditional compilation</a>.
 *
 * TODO link to specification of constant expression
 */
public class ConditionalCompilationTarget {

	private static final boolean FALSE = false;
	private static final boolean TRUE = true;

	private static void conditionFalse() {
		if (FALSE) { // assertEmpty()
			nop("then"); // assertEmpty()
		} else { // assertEmpty()
			nop("else"); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void conditionTrue() {
		if (TRUE) { // assertEmpty()
			nop("then"); // assertFullyCovered()
		} else { // assertEmpty()
			nop("else"); // assertEmpty()
		} // assertEmpty()
	}

	/**
	 * {@link #FALSE} leads to compilation error
	 */
	private static void whileLoop() {
		while (TRUE) { // assertEmpty()
			nop(); // assertNotCovered()
			break; // assertEmpty()
		}
	}

	private static void doWhileLoop() {
		do { // assertEmpty()
			nop(); // assertNotCovered()
		} while (FALSE); // assertEmpty()
	}

	private static void doWhileLoop2() {
		do { // assertEmpty()
			nop(); // assertNotCovered()
			if (TRUE) /* TODO compare when not constant */
				break; // assertEmpty()
		} while (TRUE); // assertEmpty()
	}

	private static void noInCaseOfSwitchStatement() {
		switch (42) { // assertNotCovered(2, 0)
		case 42: // assertEmpty()
			nop("42"); // assertNotCovered()
			break; // assertNotCovered()
		default: // assertEmpty()
			nop("default"); // assertNotCovered()
			break; // assertEmpty()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		conditionFalse();
		conditionTrue();
	}

}
