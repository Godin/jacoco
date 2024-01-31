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

	private record Record(Object component) {
	}

	private static void wip(Record o) {
		/* TODO unfortunate */
		if (o instanceof Record(String s)) { // assertFullyCovered(1, 3)
			nop(); // assertFullyCovered()
		} else {
			nop(); // assertFullyCovered()
		}
	}

	private record Record2(String a, String b) {
	}

	private static void switchWIP(Object p) {
		switch (p) { // assertFullyCovered(0, 3)
			case Record2(String a, String b) when a.length() == 0 -> nop(); // assertFullyCovered(0, 2)
			case Record2(String a, String b) -> nop(); // assertFullyCovered()
			default -> nop(); // assertFullyCovered()
		} // assertEmpty()
	}

	private record Record3(int i) {
	}

	private static void switchPrimitiveComponent(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
			case Record3(int x) when x > 0 -> nop(x); // assertFullyCovered(0, 2)
			case Record3(int x) -> nop(x); // assertFullyCovered()
			/* TODO unfortunate in the presence of previous line */
			default -> nop(); // assertPartlyCovered(1, 2)
		} // assertEmpty()
	}

	private static void switchPrimitiveComponent2(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
			case Point(int x, int y) when x > 0 -> nop(x + y); // assertPartlyCovered(1, 4)
			/* TODO unfortunate in the presence of next line */
			case Point(int x, int y) -> nop(x + y); // assertFullyCovered()
			default -> nop(); // assertPartlyCovered(1, 1)
		}
	}

	private record Point(int x, int y) {
	}

	private static void instanceofOperator(Object o) {
		if (o instanceof Point(int x, int y)) { // assertFullyCovered(0, 2)
			nop(x + y); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void switchStatement(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
		case Point(int x, int y) -> nop(x + y); // assertSwitchStatementLastCase()
		default -> nop(); // assertFullyCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		wip(new Record(new Object()));
		wip(new Record(""));

		switchWIP(new Object());
		switchWIP(new Record2("", ""));
		switchWIP(new Record2("a", "b"));

		switchPrimitiveComponent(new Object());
		switchPrimitiveComponent(new Record3(1));
		switchPrimitiveComponent(new Record3(-1));

		switchPrimitiveComponent2(new Object());
		switchPrimitiveComponent2(new Point(1, 2));
		switchPrimitiveComponent2(new Point(-1, -2));

		instanceofOperator(new Point(1, 2));
		instanceofOperator(new Object());

		switchStatement(new Point(1, 2));
		switchStatement(new Object());
	}

}
