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
package org.jacoco.core.test.validation.java21.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target exercises Record Patterns
 * (<a href="https://openjdk.org/jeps/440">JEP 440</a>).
 */
public class RecordPatternsTarget {

	private record Point(int x, int y) {
	}

	private static void test() {
		if (true) { // assertEmpty()
			nop(); // assertFullyCovered()
		} else {
			nop(); // assertEmpty()
		}
	}

	private static void instanceofOperator(Object o) {
		/* try replace int by byte on JDK 23 */
		if (o instanceof Point(int x, int y)) { // assertInstanceof()
			nop(x + y); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void switchStatement(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
		case Point(int x, int y) -> nop(x + y); // assertSwitchStatementLastCase()
		default -> nop(); // assertFullyCovered()
		} // assertEmpty()
	}

	record R(int c) {
	}

	private static void switchStatement2(Object o) {
		switch (o) { // assertFullyCovered(0, 2)
			case R(int c) when c > 0 -> nop(); // assertFullyCovered(0, 2)
			case R(int c) when c == 0 -> nop(); // assertFullyCovered(0, 2)
			case R(int c) -> nop(); // assertFullyCovered()
			default -> nop(); // assertFullyCovered(0, 3)
		}
	}

	public static void main(String[] args) {
		switchStatement2(new R(1));
		switchStatement2(new R(0));
		switchStatement2(new R(-1));
		switchStatement2(new Object());

		test();

		instanceofOperator(new Point(1, 2));
		instanceofOperator(new Object());

		switchStatement(new Point(1, 2));
		switchStatement(new Object());
	}

}
