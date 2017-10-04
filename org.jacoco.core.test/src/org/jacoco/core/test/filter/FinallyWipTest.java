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
import org.jacoco.core.test.filter.targets.FinallyWip;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

/**
 * FIXME
 */
public class FinallyWipTest extends ValidationTestBase {

	public FinallyWipTest() {
		super(FinallyWip.class);
	}

	@Test
	public void test() {
		assertLine("wip", ICounter.FULLY_COVERED);
	}

}
