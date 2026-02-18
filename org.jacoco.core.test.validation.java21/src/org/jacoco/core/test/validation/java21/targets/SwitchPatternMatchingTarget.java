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

import org.jacoco.core.test.validation.targets.Stubs;

/**
 * This target exercises pattern matching for switch
 * (<a href="https://openjdk.org/jeps/441">JEP 441</a>).
 */
public class SwitchPatternMatchingTarget {

	private static void example(Object o) {
		switch (o) { // assertFullyCovered(1, 2)
		case String s when s.length() == 0 -> // assertFullyCovered(0, 2)
			nop(s); // assertFullyCovered()
		case String s -> // assertFullyCovered()
			nop(s); // assertFullyCovered()
		default -> // assertEmpty()
			nop(); // assertNotCovered()
		}
	}

	private static void exhaustive(Sealed o) {
		switch (o) { // assertFullyCovered(0, 2)
		case Sealed.A a -> // assertFullyCovered()
			nop(a); // assertFullyCovered()
		case Sealed.B b -> // assertFullyCovered()
			nop(b); // assertFullyCovered()
		} // assertEmpty()
	}

	/**
	 * FIXME
	 */
	private static void handwrittenMatchException(Object o) {
		switch (o) { // assertPartlyCovered()
		case String s -> // assertNotCovered()
			nop(s); // assertNotCovered()
		default -> // assertEmpty()
			throw new MatchException(null, null); // assertEmpty()
		}
	}

	private static void handwrittenMatchException() {
		try {
			handwrittenMatchException(new Object());
		} catch (MatchException ignore) {
			/* expected */
		}
	}

	private static void enumSwitch(Stubs.Enum o) {
		switch (o) { // assertNotCovered(2, 0)
		case Stubs.Enum e // assertNotCovered(2, 0)
		when e == Stubs.Enum.A -> // assertEmpty()
			nop(e); // assertNotCovered()
		case Stubs.Enum e -> // assertNotCovered()
			nop(e); // assertNotCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		handwrittenMatchException();

		example("");
		example("a");

		exhaustive(new Sealed.A(""));
		exhaustive(new Sealed.B(""));
	}

}
