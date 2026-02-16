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
package org.jacoco.core.test.validation.java22.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * TODO https://openjdk.org/jeps/530
 */
public class SwitchBooleanTarget {

	private static void example(Boolean b) {
		switch (b) { // assertFullyCovered(0, 3)
		case true -> nop("true"); // assertFullyCovered()
		case false -> nop("false"); // assertFullyCovered()
		case null -> nop("null"); // assertFullyCovered()
		} // assertEmpty()
	}

	/**
	 * No conditional compilation for switch
	 */
	private static void cond() {
		switch (true) { // assertNotCovered(2, 0)
		case true -> nop("true");
		case false -> nop("false");
		}
	}

	public static void main(String[] args) {
		example(true);
		example(false);
		example(null);
	}

}
