/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinJvmSyntheticFileTargetKt;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinJvmSyntheticFileTargetKt}.
 */
public class KotlinJvmSyntheticFileTest extends ValidationTestBase {

	public KotlinJvmSyntheticFileTest() {
		super(KotlinJvmSyntheticFileTargetKt.class);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(1);
	}

}
