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
package org.jacoco.core.test.validation.java21.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This target exercises Record Patterns
 * (<a href="https://openjdk.org/jeps/440">JEP 440</a>).
 *
 * TODO test multiple record components, guards, exhaustive
 */
public class RecordPatternsTarget {

	private record Point(int x, int y) {
	}

	private static void instanceofOperator(Object o) {
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

	record Container(Object component) {
	}

	private static String switchStatement1(Object o) {
		return switch (o) { // assertFullyCovered(0, 7)
		case Container(String c) -> // fullyJavac() fullyEcj(0, 2)
			"Container(String)"; // assertFullyCovered()
		case Container(Integer c) -> // fullyJavac() fullyEcj(0, 2)
			"Container(Integer)"; // assertFullyCovered()
		case Container(Container(String c)) -> // fullyJavac() partlyEcj(1, 3)
			"Container(Container(String))"; // assertFullyCovered()
		case Container(Container(Integer c)) -> // fullyJavac() partlyEcj(2, 2)
			"Container(Container(Integer))"; // assertFullyCovered()
		case String c -> // assertFullyCovered()
			"String"; // assertFullyCovered()
		case Integer i -> // assertFullyCovered()
			"Integer"; // assertFullyCovered()
		default -> // assertEmpty()
			"default"; // assertFullyCovered()
		}; // assertEmpty()
	}

	/**
	 * Outermost is LOOKUPSWITCH.
	 */
	private static String outerLookupSwitch(Object o) {
		return switch (o) { // assertFullyCovered(0, 4)
		case Container(String c) -> // fullyJavac() fullyEcj(0, 2)
			"Container(String)"; // assertFullyCovered()
		case Container(Integer c) -> // fullyJavac() partlyEcj(1, 1)
			"Container(Integer)"; // assertFullyCovered()
		case String s -> // assertFullyCovered()
			"String"; // assertFullyCovered()
		default -> // assertEmpty()
			"default"; // assertFullyCovered()
		}; // assertEmpty()
	}

	private static String innerInstanceof(Object o) {
		return switch (o) { // assertNotCovered(3, 0)
		case Container(String c) -> // assertNotCovered(2, 0)
			"Container(String)"; // assertNotCovered()
		case String s -> // assertNotCovered()
			"String"; // assertNotCovered()
		default -> // assertEmpty()
			"default"; // assertNotCovered()
		}; // assertEmpty()
	}

	/**
	 * FIXME different for JDK 22 (0,2) at switch (0,3) at case Sealed.A(Object)
	 */
	private static String exhaustive(Sealed o) {
		return switch (o) { // assertFullyCovered(0, 4)
		case Sealed.A(String c) -> // fullyJavac() fullyEcj(0, 2)
			"A(String)"; // assertFullyCovered()
		case Sealed.A(Integer c) -> // fullyJavac() fullyEcj(0, 2)
			"A(Integer)"; // assertFullyCovered()
		case Sealed.A(Object c) -> // fullyJavac() partlyEcj(0, 0)
			"A(Object)"; // assertFullyCovered()
		case Sealed.B(Object c) -> // fullyJavac() partlyEcj(0, 0)
			"B"; // assertFullyCovered()
		}; // assertEmpty()
	}

	public static void main(String[] args) {
		instanceofOperator(new Point(1, 2));
		instanceofOperator(new Object());

		switchStatement(new Point(1, 2));
		switchStatement(new Object());

		switchStatement1(new Container("String"));
		switchStatement1(new Container(Integer.valueOf(42)));
		switchStatement1(new Container(new Container("String")));
		switchStatement1(new Container(new Container(Integer.valueOf(42))));
		switchStatement1("String");
		switchStatement1(Integer.valueOf(42));
		switchStatement1(new Object());

		outerLookupSwitch(new Container("String"));
		outerLookupSwitch(new Container(Integer.valueOf(42)));
		outerLookupSwitch("String");
		outerLookupSwitch(new Object());

		exhaustive(new Sealed.A(""));
		exhaustive(new Sealed.A(Integer.valueOf(42)));
		exhaustive(new Sealed.A(new Object()));
		exhaustive(new Sealed.B(""));
	}

}
