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

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.i1;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

import org.objectweb.asm.Label;

/**
 * Visually result is misleading and counters are underestimated.
 */
public class ImplicitExceptions {

	/**
	 * Exception on a first line of a method. Even if successor of probe
	 * considered as executed, this case requires probe at the beginning of
	 * method. For example <a href=
	 * "https://github.com/junit-team/junit5/blob/r5.0.0-M3/junit-jupiter-api/src/main/java/org/junit/jupiter/api/Assertions.java#L54">
	 * JUnit's Assertions class</a> or usage of
	 * <a href= "https://github.com/google/guava/wiki/PreconditionsExplained">
	 * Guava's Preconditions</a>.
	 */
	private static void methodFirstLine() {
		ex(""); // $line-methodFirstLine.exception$
		nop(); // $line-methodFirstLine.after$
	}

	/**
	 * Contrary to {@link #tryBlockFirstLine()} and similarly to
	 * {@link #methodFirstLine()} requires probe at the beginning of method.
	 */
	private static void methodFirstLineTryBlock() {
		try {
			ex(""); // $line-methodFirstLineTryBlock.exception$
			nop(); // $line-methodFirstLineTryBlock.after$
		} catch (Exception e) {
			nop(); // $line-methodFirstLineTryBlock.catchBlock$
		}
	}

	/**
	 * Exception on a first line of a try-block that is <strong>not</strong> at
	 * the beginning of a method. Doesn't require any additional probe.
	 *
	 * @see org.jacoco.core.internal.flow.LabelFlowAnalyzer#visitTryCatchBlock(Label,
	 *      Label, Label, String)
	 */
	private static void tryBlockFirstLine() {
		String msg = ""; // $line-tryBlockFirstLine.before$
		try {
			ex(msg); // $line-tryBlockFirstLine.exception$
			nop(); // $line-tryBlockFirstLine.after$
		} catch (Exception e) {
			nop();
		}
	}

	/**
	 * Exception on a first line of a finally-block. TODO add explanation
	 */
	private static void finallyBlockFirstLine() {
		try {
			throw new RuntimeException();
		} finally {
			ex(""); // $line-finallyBlockFirstLine.exception$
		}
	}

	/**
	 * Exception on a first line of a catch-block. TODO add explanation
	 */
	private static void catchBlockFirstLine() {
		try {
			throw new RuntimeException();
		} catch (Exception e) {
			ex(""); // $line-catchBlockFirstLine.exception$
		}
	}

	/**
	 * Exception on a line without method invocation. Even if successor of probe
	 * considered as executed, this case requires probe at each line.
	 */
	private static void sequence(int[] a) {
		nop(); // $line-sequence.twoLinesPriorToException$
		nop(i1()); // $line-sequence.oneLinePriorToException$
		a[0] = 0; // $line-sequence.exception$
	} // $line-sequence.after$

	/**
	 * Exception on a line with branches.
	 */
	private static void branchesLine() {
		nop();
		// For the following cases currently there are no probes that can
		// identify branch that was executed:
		try {
			nop(t() && exF()); // $line-branchesLine.exception1$
		} catch (Exception e) {
		}
		try {
			nop(t() && t() && exF()); // $line-branchesLine.exception2$
		} catch (Exception e) {
		}
		// But there is for the following case:
		try {
			nop((f() || t()) && exF()); // $line-branchesLine.exception3$
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		try {
			methodFirstLine();
		} catch (Exception e) {
		}

		methodFirstLineTryBlock();
		tryBlockFirstLine();

		try {
			finallyBlockFirstLine();
		} catch (Exception e) {
		}
		try {
			catchBlockFirstLine();
		} catch (Exception e) {
		}

		try {
			sequence(null);
		} catch (Exception e) {
		}

		branchesLine();
	}

	private static boolean exF() {
		throw new RuntimeException();
	}

	private static void ex(String message) {
		throw new RuntimeException(message);
	}

}
