/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinDelegationTarget;
import org.junit.Test;

/**
 * Test of <code>???</code>.
 */
public class KotlinDelegationTest extends ValidationTestBase {

	public KotlinDelegationTest() {
		super(KotlinDelegationTarget.class);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(5);
	}

}
