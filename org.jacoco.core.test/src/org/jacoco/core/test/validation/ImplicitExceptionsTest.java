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
package org.jacoco.core.test.validation;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.ImplicitExceptions;
import org.junit.Test;

public class ImplicitExceptionsTest extends ValidationTestBase {

	public ImplicitExceptionsTest() {
		super(ImplicitExceptions.class);
	}

	/**
	 * @see ImplicitExceptions#methodFirstLine()
	 */
	@Test
	public void methodFirstLine() {
		assertLine("methodFirstLine.exception", ICounter.NOT_COVERED);
		assertLine("methodFirstLine.after", ICounter.NOT_COVERED);
	}

	/**
	 * @see ImplicitExceptions#methodFirstLineTryBlock()
	 */
	@Test
	public void methodFirstLineTryBlock() {
		assertLine("methodFirstLineTryBlock.exception", ICounter.NOT_COVERED);
		assertLine("methodFirstLineTryBlock.after", ICounter.NOT_COVERED);
		assertLine("methodFirstLineTryBlock.catchBlock",
				ICounter.FULLY_COVERED);
	}

	/**
	 * @see ImplicitExceptions#tryBlockFirstLine()
	 */
	@Test
	public void tryBlockFirstLine() {
		assertLine("tryBlockFirstLine.exception", ICounter.PARTLY_COVERED);
	}

	/**
	 * @see ImplicitExceptions#finallyBlockFirstLine()
	 */
	@Test
	public void finallyBlockFirstLine() {
		assertLine("finallyBlockFirstLine.exception", ICounter.NOT_COVERED);
	}

	/**
	 * @see ImplicitExceptions#catchBlockFirstLine()
	 */
	@Test
	public void catchBlockFirstLine() {
		assertLine("catchBlockFirstLine.exception", ICounter.PARTLY_COVERED);
	}

	/**
	 * @see ImplicitExceptions#sequence(int[])
	 */
	@Test
	public void sequence() {
		assertLine("sequence.twoLinesPriorToException", ICounter.FULLY_COVERED);
		assertLine("sequence.oneLinePriorToException", ICounter.PARTLY_COVERED);
		assertLine("sequence.exception", ICounter.NOT_COVERED);
		assertLine("sequence.after", ICounter.NOT_COVERED);
	}

	/**
	 * @see ImplicitExceptions#branchesLine()
	 */
	@Test
	public void branchesLine() {
		assertLine("branchesLine.exception1", ICounter.PARTLY_COVERED, 4, 0);
		assertLine("branchesLine.exception2", ICounter.PARTLY_COVERED, 6, 0);
		assertLine("branchesLine.exception3", ICounter.PARTLY_COVERED, 4, 2);
	}

}
