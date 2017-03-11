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
import org.jacoco.core.test.validation.targets.Synchronized;
import org.junit.Test;

public class SynchronizedTest extends ValidationTestBase {

	public SynchronizedTest() {
		super(Synchronized.class);
	}

	@Test
	public void testCoverageResult() {
		assertLine("monitorenter", ICounter.FULLY_COVERED);
		assertLine("monitorexit", ICounter.PARTLY_COVERED);

		assertLine("explicitException.monitorenter", ICounter.FULLY_COVERED);
		assertLine("explicitException.exception", ICounter.FULLY_COVERED);
		assertLine("explicitException.monitorexit", ICounter.FULLY_COVERED);
		assertLine("explicitException.after", ICounter.EMPTY);

		assertLine("implicitException.monitorenter", ICounter.FULLY_COVERED);
		assertLine("implicitException.exception", ICounter.NOT_COVERED);
		assertLine("implicitException.monitorexit", ICounter.PARTLY_COVERED);
		assertLine("implicitException.after", ICounter.NOT_COVERED);
	}

}
