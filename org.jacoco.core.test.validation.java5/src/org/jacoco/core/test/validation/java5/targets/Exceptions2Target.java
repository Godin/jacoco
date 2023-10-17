/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.*;

/**
 * TODO
 */
public class Exceptions2Target {

	private static void noProbeAtTheBeginningOfMethod() {
		ex(); // assertNotCovered()
	} // assertNotCovered()

	/**
	 * @see ExceptionsTarget#implicitException()
	 */
	private static void example() {
		avoidFirstLineIssue(); // assertFullyCovered()
		ex(); // assertFullyCovered()
	} // assertNotCovered()

	/**
	 * @see org.jacoco.core.internal.flow.LabelFlowAnalyzer#visitTryCatchBlock(org.objectweb.asm.Label,
	 *      org.objectweb.asm.Label, org.objectweb.asm.Label, String)
	 * @see ExceptionsTarget#implicitExceptionTryCatch()
	 */
	private static void tryBlock() {
		avoidFirstLineIssue(); // assertFullyCovered()
		try { // assertEmpty()
			ex(); // assertFullyCovered()
		} catch (StubException e) { // assertFullyCovered()
			nop(); // assertFullyCovered()
			/* TRY BLOCK END: */
		} // assertNotCovered()
	} // assertFullyCovered()

	/**
	 * @see org.jacoco.core.internal.flow.LabelFlowAnalyzer#visitTryCatchBlock(org.objectweb.asm.Label,
	 *      org.objectweb.asm.Label, org.objectweb.asm.Label, String)
	 */
	private static void catchBlock() {
		avoidFirstLineIssue(); // assertFullyCovered()
		try { // assertEmpty()
			ex(); // assertFullyCovered()
		} catch (StubException e) { // assertFullyCovered()
			ex(); // assertFullyCovered()
			/* TRY BLOCK END: */
		} // assertNotCovered()
	} // assertNotCovered()

	private static void finallyBlock() {
		avoidFirstLineIssue(); // assertFullyCovered()
		try { // assertEmpty()
			throw new StubException(); // assertFullyCovered()
		} finally { // assertEmpty()
			avoidFirstLineIssue(); // assertFullyCovered()
			ex(); // assertFullyCovered()
			/* FILTERED by FinallyFilter: */
		} // assertEmpty()
	}

	/**
	 * First line is due to https://github.com/jacoco/jacoco/pull/321
	 *
	 * same as {@link #another()}
	 */
	private static void branches(boolean f) {
		if (f) { // assertFullyCovered(1, 1)
			/* FOLLOWED by goto: */
			ex(); // assertPartlyCovered()
		} else { // assertEmpty()
			ex(); /* TODO issue#321 */ // assertNotCovered()
		} // assertEmpty()
	} // assertNotCovered()

	private static void branches_true() {
		if (t()) { // assertFullyCovered(1, 1)
			/* FOLLOWED by goto: */
			ex(); // assertPartlyCovered()
		} else { // assertEmpty()
			ex(); // assertNotCovered()
		} // assertEmpty()
	} // assertNotCovered()

	private static void branches_else() {
		if (f()) { // assertNotCovered(2, 0)
			ex(); // assertNotCovered()
		} else { // assertEmpty()
			ex(); /* TODO issue#321 */ // assertNotCovered()
		} // assertEmpty()
	} // assertNotCovered()

	/**
	 * TODO why there is difference with {@link #after_else()} ?
	 *
	 * same as {@link #another()}
	 */
	private static void after_if() {
		if (f()) { // assertFullyCovered(1, 1)
			nop(); // assertNotCovered()
		} // assertEmpty()
		ex(); // assertFullyCovered()
	} // assertNotCovered()

	private static void after_else() {
		if (f()) { // assertFullyCovered(1, 1)
			nop(); // assertNotCovered()
		} else { // assertEmpty()
			nop(); // assertFullyCovered()
		} // assertEmpty()
		ex(); // assertFullyCovered()
	} // assertNotCovered()

	private static void condition() {
		avoidFirstLineIssue(); // assertFullyCovered()
		if (boolEx()) { // assertPartlyCovered(2, 0)
			nop(); // assertNotCovered()
		} // assertEmpty()
	} // assertNotCovered()

	/**
	 * https://github.com/jacoco/jacoco/pull/321
	 */
	private static void another() {
		if (f()) {
			return;
		}
		ex(); /* TODO issue#321 */ // assertNotCovered()
	}

	public static void main(String[] args) {
		try {
			another();
		} catch (StubException e) {
		}

		try {
			condition();
		} catch (StubException e) {
		}

		try {
			after_if();
		} catch (StubException e) {
		}
		try {
			after_else();
		} catch (StubException e) {
		}

		try {
			branches(true);
		} catch (StubException e) {
		}
		try {
			branches(false);
		} catch (StubException e) {
		}
		try {
			branches_else();
		} catch (StubException e) {
		}
		try {
			branches_true();
		} catch (StubException e) {
		}

		try {
			noProbeAtTheBeginningOfMethod();
		} catch (StubException e) {
		}

		try {
			example();
		} catch (StubException e) {
		}

		tryBlock();

		try {
			catchBlock();
		} catch (StubException e) {
		}

		try {
			finallyBlock();
		} catch (StubException e) {
		}
	}

	private static boolean boolEx() {
		throw new StubException();
	}

	/**
	 * @see #noProbeAtTheBeginningOfMethod()
	 */
	private static void avoidFirstLineIssue() {
	}

}
