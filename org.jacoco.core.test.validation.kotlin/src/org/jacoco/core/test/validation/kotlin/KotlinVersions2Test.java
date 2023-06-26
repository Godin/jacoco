/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.test.validation.kotlin.targets.KotlinCoroutineTarget;
import org.jacoco.core.test.validation.kotlin.targets.KotlinWhenExpressionTarget;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class KotlinVersions2Test extends KotlinValidationTestBase {

	@Parameterized.Parameters(name = "Kotlin {0}")
	public static Iterable<?> data() {
		return Kotlin.SUPPORTED_VERSIONS;
	}

	public KotlinVersions2Test(final String kotlinVersion) throws Exception {
		super(kotlinVersion, KotlinWhenExpressionTarget.class);
	}

}
