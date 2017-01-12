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

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * <!-- TODO extract into documentation -->
 *
 * <h1>Problem statement</h1>
 *
 * <pre>
 * Recording of coverage for the case of exceptions - is a trade off:
 *
 * Each method invocation potentially can throw an exception.
 * Insertion of probes for each will lead to dramatic decrease of
 * performance and increase of memory consumption.
 *
 * While absence of probes leads to underestimation,
 * to results that users can't understand easily,
 * and hence they consider this as undesired behavior and bug.
 *
 * So we try to find balance between two.
 * </pre>
 *
 * <!-- TODO what about cases where exception without method invocations? -->
 *
 * <h1>Current implementation</h1>
 *
 * <pre>
 * Probe at the beginning of a line marking previous instructions as executed.
 * </pre>
 *
 * <h1>Current idea</h1>
 *
 * <pre>
 * Probe before the last method invocation instruction on a line.
 * </pre>
 *
 * <h1>Another idea</h1>
 *
 * <pre>
 * Probe at the beginning of a line marking next instruction as executed.
 * This supposed to mark line as partly covered.
 * </pre>
 *
 * <!-- TODO how this affects counter "covered lines"? -->
 */
public class Exceptions {

	/**
	 * Declares that has return value, but always throws a
	 * {@link RuntimeException}.
	 */
	private static int exReturn() {
		throw new RuntimeException();
	}

	private static int exReturn(boolean p) {
		nop(p);
		throw new RuntimeException();
	}

	private static void ex() {
		throw new RuntimeException();
	}

	private static void ex(boolean p) {
		nop(p);
		throw new RuntimeException();
	}

	/** Last instruction on a line, no instructions prior, first line. */
	private static void case1() { // $line-case-1$
		// e.g. first implementation of a method in Test-driven development
		ex(); // $line-case-1_0$
	} // $line-case-1_1$

	/** Last instruction on a line, instructions prior, first line. */
	private static void case1_1(boolean p) { // $line-case-1_1_0$
		// e.g. Guava's Preconditions.checkState
		ex(p); // $line-case-1_1_1$
	} // $line-case-1_1_2$

	/**
	 * Probe should be inserted even if method invocation is not last
	 * instruction on a line (e.g. as in this case where {@code pop} follows
	 * invocation), otherwise previous instructions up to previous probe won't
	 * be marked as covered.
	 */
	private static void case2() {
		int x = 0; // $line-case-2$
		// IntelliJ marks next line as fully covered (overestimation)
		exReturn(); // $line-case-2_1$
		nop(x); // $line-case-2_2$
	}

	/**
	 * Not last instruction on a line, no instructions prior, first line.
	 *
	 * @see #case3_workaround() kind of workaround that requires javac 8
	 */
	private static void case3() { // $line-case-3$
		nop(exReturn()); // $line-case-3_0$
		// IntelliJ marks previous line as fully covered (overestimation),
		// but not the following
	} // $line-case-3_1$

	/**
	 * Not last instruction on a line, instructions prior, first line.
	 */
	private static void case3_common(boolean p) { // $line-case-3_common$
		nop(exReturn(p)); // $line-case-3_common_0$
		// IntelliJ marks previous line as fully covered (overestimation),
		// but not the following
	} // $line-case-3_common_1$

	/**
	 * With javac 7: not last instruction on a line, no instructions prior,
	 * first line. With javac 8: last instruction on a line, no instructions
	 * prior, second line.
	 */
	private static void case3_workaround() { // $line-case-3_workaround$
		nop( // $line-case-4_1$
				// with javac 8 IntelliJ marks next line as fully covered
				// (overestimation), but not previous, and with javac 7 marks
				// previous line as fully covered, but not next
				exReturn() // $line-case-4_2$
		);
	}

	static class Chain {
		void ex() {
			throw new RuntimeException();
		}
	}

	/**
	 * Last instruction on a line, instructions prior, first line.
	 */
	private static void case5() {
		new Chain().ex(); // $line-case-5$
	}

	/**
	 * With javac 7 same as {@link #case5()}. With javac 8: last instruction on
	 * a line, no instructions prior, second line.
	 */
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
			case1_1(true);
		} catch (Exception e) {
		}

		try {
			case2();
		} catch (Exception e) {
		}
		try {
			case3();
		} catch (Exception e) {
		}
		try {
			case3_common(true);
		} catch (Exception e) {
		}
		try {
			case3_workaround();
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
