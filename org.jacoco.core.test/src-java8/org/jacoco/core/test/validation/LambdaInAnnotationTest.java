/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.test.validation.targets.LambdaInAnnotationTarget;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;

/**
 * Tests a constant with a lambda value in an annotation.
 */
public class LambdaInAnnotationTest extends ValidationTestBase {

	public LambdaInAnnotationTest() {
		super("src-java8", LambdaInAnnotationTarget.class);
	}

	@Test
	public void testCoverageResult() {
		// TODO(Godin): JaCoCo should be disabled for "targets" in order to
		// count number of fields in original

		// Coverage of lambda body
		assertLine("lambdabody", ICounter.FULLY_COVERED);
	}

}
