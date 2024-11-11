/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java7.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

public class ExampleTarget {

	private static void switchByString(String s) {
		switch (s) { // assertFullyCovered(3, 1)
		case "a":
			nop("a");
			break;
		case "b":
			nop("b");
			break;
		case "c":
			nop("c");
			break;
		}
	}

	private static void switchByInt(int i) {
		switch (i) { // assertFullyCovered(3, 1)
		case 1:
			nop("a");
			break;
		case 2:
			nop("b");
			break;
		case 3:
			nop("c");
			break;
		}
	}

	public static void main(String[] args) {
		switchByString("b");
		switchByInt(2);
	}

}
