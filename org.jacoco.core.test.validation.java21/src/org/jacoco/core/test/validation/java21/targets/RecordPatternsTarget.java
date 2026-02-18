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
 * TODO test guards
 */
public class RecordPatternsTarget {

	private record Point(int x, int y) {
	}

	private static void instanceofOperator(Object o) {
		if (o instanceof Point(int x, int y)) { // assertInstanceof()
			nop(x + y); // assertFullyCovered()
		} // assertEmpty()
	}

	/**
	 * https://www.reddit.com/r/java/comments/1q8o54y/project_amber_status_update_constant_patterns_and/
	 * https://mail.openjdk.org/pipermail/amber-spec-experts/2026-January/004306.html
	 */
	private static void decompose(Container o) {
		if (!(o instanceof Container(Object component))) {
			return;
		}
		nop(component);
	}

	private static void switchStatement(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
		case Point(int x, int y) -> nop(x + y); // assertSwitchStatementLastCase()
		default -> nop(); // assertFullyCovered()
		} // assertEmpty()
	}

	record Container(Object component) {
	}

	/**
	 * TODO is there diff between (Object o) and (Container o) ? see
	 * {@link #wip(Container)}
	 */
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

	/**
	 * Modified {@link #outerLookupSwitch(Object)}
	 */
	private static String wip(Container o) {
		return switch (o) { // assertFullyCovered(1, 3)
		case Container(String c) -> // fullyJavac() fullyEcj(0, 2)
			"Container(String)"; // assertFullyCovered()
		case Container(Integer c) -> // fullyJavac() partlyEcj(1, 1)
			"Container(Integer)"; // assertFullyCovered()
		case null -> // assertEmpty()
			"null"; // assertNotCovered()
		default -> // assertEmpty()
			"default"; // assertFullyCovered()
		}; // assertEmpty()
	}

	/**
	 * FIXME
	 */
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
			"B(Object)"; // assertFullyCovered()
		}; // assertEmpty()
	}

	record Ex(Sealed c) {
	}

	/**
	 * TODO bytecode seems more stable across JDK versions compared to
	 * {@link #exhaustive(Sealed)}
	 */
	private static String exhaustive2(Ex o) {
		return switch (o) { // assertFullyCovered(0, 2)
		case Ex(Sealed.A c) -> // fullyJavac() fullyEcj(0, 2)
			""; // assertFullyCovered()
		case Ex(Sealed.B c) -> // fullyJavac() partlyEcj(1, 1)
			""; // assertFullyCovered()
		}; // assertEmpty()
	}

	/**
	 * TODO bytecode seems more stable across JDK versions compared to
	 * {@link #exhaustive(Sealed)}
	 */
	private static String exhaustive4(Sealed3 o) {
		return switch (o) { // assertFullyCovered(0, 4)
		case Sealed3.A(Sealed2.A(String c)) -> // fullyJavac() fullyEcj(0, 2)
			""; // assertFullyCovered()
		case Sealed3.A(Sealed2.B(Integer c)) -> // fullyJavac() partlyEcj(1, 1)
			""; // assertFullyCovered()
		case Sealed3.B(Sealed2.A(String c)) -> // fullyJavac() fullyEcj(0, 2)
			""; // assertFullyCovered()
		case Sealed3.B(Sealed2.B(Integer c)) -> // fullyJavac() partlyEcj(1, 1)
			""; // assertFullyCovered()
		}; // assertEmpty()
	}

	/**
	 * FIXME
	 */
	private static String handwrittenMatchException(Object o) {
		return switch (o) { // assertPartlyCovered(2, 0)
		case Container(String c) -> // assertNotCovered()
			"Container(String)"; // assertNotCovered()
		case Container(Integer c) -> // assertNotCovered()
			"Container(Integer)"; // assertNotCovered()
		default -> // assertEmpty()
			throw new MatchException(null, null); // assertNotCovered()
		};
	}

	private static void handwrittenMatchException() {
		try {
			handwrittenMatchException(new Object());
		} catch (MatchException ignore) {
			/* expected */
		}
	}

	private static void exhaustive4() {
		exhaustive4(new Sealed3.A(new Sealed2.A("")));
		exhaustive4(new Sealed3.A(new Sealed2.B(42)));
		exhaustive4(new Sealed3.B(new Sealed2.A("")));
		exhaustive4(new Sealed3.B(new Sealed2.B(42)));
	}

	/**
	 * Smallest possible exhaustive, but/and without nested.
	 */
	private static String exhaustive3(Ex o) {
		return switch (o) { // assertFullyCovered()
		case Ex(Object c) -> // fullyJavac() partlyEcj(0, 0)
			""; // assertFullyCovered()
		}; // assertEmpty()
	}

	record Container2(Object component1, Object component2) {
	}

	/**
	 * inner lookup switch
	 */
	private static String wip(Container2 o) {
		return switch (o) { // assertNotCovered(3, 0)
		case Container2(String c1, String c2) -> // assertNotCovered()
			""; // assertNotCovered()
		case Container2(String c1, Integer c2) -> // assertNotCovered()
			""; // assertNotCovered()
		default -> // assertEmpty()
			"default"; // assertNotCovered()
		}; // assertEmpty()
	}

	/**
	 * FIXME inner instanceof
	 */
	private static String wip2(Container2 o) {
		return switch (o) { // assertNotCovered(4, 0)
		case Container2(String c1, String c2) -> // assertNotCovered()
			""; // assertNotCovered()
		case Container2(String c1, Integer c2) -> // assertNotCovered()
			""; // assertNotCovered()
		case Container2(Integer c1, Integer c2) -> // assertNotCovered(2, 0)
			""; // assertNotCovered()
		default -> // assertEmpty()
			"default"; // assertNotCovered()
		}; // assertEmpty()
	}

	private static String wip3(Container2 o) {
		return switch (o) { // assertNotCovered(5, 0)
		case Container2(String c1, String c2) -> // assertNotCovered()
			""; // assertNotCovered()
		case Container2(String c1, Integer c2) -> // assertNotCovered()
			""; // assertNotCovered()
		case Container2(Integer c1, Integer c2) -> // assertNotCovered()
			""; // assertNotCovered()
		case Container2(Integer c1, String c2) -> // assertNotCovered()
			""; // assertNotCovered()
		default -> // assertEmpty()
			"default"; // assertNotCovered()
		}; // assertEmpty()
	}

	public static void main(String[] args) {
		handwrittenMatchException();

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

		wip(new Container("String"));
		wip(new Container(Integer.valueOf(42)));
		wip(new Container(new Object()));

		exhaustive2(new Ex(new Sealed.A("")));
		exhaustive2(new Ex(new Sealed.B("")));

		exhaustive3(new Ex(new Sealed.A("")));

		exhaustive(new Sealed.A(""));
		exhaustive(new Sealed.A(Integer.valueOf(42)));
		exhaustive(new Sealed.A(new Object()));
		exhaustive(new Sealed.B(""));

		exhaustive4();
	}

}
