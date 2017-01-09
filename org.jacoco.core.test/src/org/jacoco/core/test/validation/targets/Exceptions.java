/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

public class Exceptions {

	/**
	 * Declares that has return value, but always throws a
	 * {@link RuntimeException}.
	 */
	private static int exReturn() {
		throw new RuntimeException();
	}

	private static void case1() {
		ex(); // $line-case-1$
	} // $line-case-1_1$

	/**
	 * Probe should be inserted even if method invocation is not last
	 * instruction on a line (e.g. as in this case where {@code pop} follows
	 * invocation), otherwise previous instructions up to previous probe won't
	 * be marked as covered.
	 */
	private static void case2() {
		int x = 0; // $line-case-2$
		// IntelliJ marks next line as fully covered
		exReturn(); // $line-case-2_1$
		nop(x); // $line-case-2_2$
	}

	private static void case3() {
		// IntelliJ marks next line as fully covered
		nop(exReturn()); // $line-case-3$
	}

	private static void case4() {
		nop( // $line-case-4_1$
				// IntelliJ marks next line as fully covered, but not previous
				exReturn() // $line-case-4_2$
		);
	}

	static class Chain {
		void ex() {
			throw new RuntimeException();
		}
	}

	private static void case5() {
		new Chain().ex(); // $line-case-5$
	}

	private static void case6() {
		new Chain() // $line-case-6_1$
				.ex(); // $line-case-6_2$
	}

	public static void main(String[] args) {
		try {
			case1();
		} catch (Exception e) {
			nop(); // $line-case-1_2$
		} // $line-case-1_3$
		nop(); // $line-case-1_4$

		try {
			case2();
		} catch (Exception e) {
		}
		try {
			case3();
		} catch (Exception e) {
		}
		try {
			case4();
		} catch (Exception e) {
		}
		try {
			case5();
		} catch (Exception e) {
		}
		try {
			case6();
		} catch (Exception e) {
		}
	}

}
