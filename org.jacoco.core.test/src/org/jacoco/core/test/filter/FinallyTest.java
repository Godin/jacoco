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
package org.jacoco.core.test.filter;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.filter.targets.Finally;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

/**
 * Test of filtering of duplicated bytecode that is generated for finally block.
 */
public class FinallyTest extends ValidationTestBase {

	public FinallyTest() {
		super(Finally.class);
	}

	/**
	 * @see Finally#beforeEndOfWhile()
	 * @see Finally#beforeEndOfDo()
	 */
	@Test
	public void beforeEndOfLoop() {
		assertLine("beforeEndOfWhile.finallyBlock.1", ICounter.FULLY_COVERED);
		assertLine("beforeEndOfWhile.finallyBlock.2",
				isJDKCompiler ? ICounter.PARTLY_COVERED
						: ICounter.FULLY_COVERED);

		assertLine("beforeEndOfDo.finallyBlock", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#wip()}
	 */
	@Test
	public void wip() {
		assertLine("wip",
				isJDKCompiler ? ICounter.EMPTY : ICounter.FULLY_COVERED);
		assertLine("wip.finallyBlock", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Finally#wip2()}
	 */
	@Test
	public void wip2() {
		assertLine("wip2", ICounter.PARTLY_COVERED);
	}

	/**
	 * {@link Finally#emptyCatch()}
	 */
	@Test
	public void emptyCatch() {
		assertLine("emptyCatchExecuted.finallyBlock", ICounter.PARTLY_COVERED);
	}

	/**
	 * {@link Finally#catchFinally()}
	 */
	@Test
	public void catchNotExecuted() {
		assertLine("catchFinally.finallyBlock", ICounter.FULLY_COVERED);
	}

	@Test
	public void testCoverageResult() {
		// assertLine("xxx", ICounter.PARTLY_COVERED, 0, 0);

		if (isJDKCompiler) {
			assertLine("motivation.if", ICounter.FULLY_COVERED, 0, 2);
		} else {
			assertLine("motivation.if", ICounter.FULLY_COVERED, 2, 2);
		}

		assertLine("test.finallyBlock", isJDKCompiler ? ICounter.FULLY_COVERED
				: ICounter.PARTLY_COVERED);
		assertLine("test.after", ICounter.FULLY_COVERED);

		assertLine("nested", isJDKCompiler ? ICounter.FULLY_COVERED
				: ICounter.PARTLY_COVERED);

		assertLine("alwaysCompletesAbruptly",
				isJDKCompiler ? ICounter.PARTLY_COVERED
						: ICounter.FULLY_COVERED);
	}

}
