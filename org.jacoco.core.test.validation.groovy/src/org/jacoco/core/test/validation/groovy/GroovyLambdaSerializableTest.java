/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.groovy;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.HashSet;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovyLambdaSerializableTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link GroovyLambdaSerializableTarget}.
 */
public class GroovyLambdaSerializableTest extends ValidationTestBase {

	public GroovyLambdaSerializableTest() {
		super(GroovyLambdaSerializableTarget.class);
	}

	@Test
	public void compiler_should_generate_synthetic_deserializeLambda() {
		final HashSet<String> names = new HashSet<String>();
		for (final Method method : GroovyLambdaSerializableTarget.class
				.getDeclaredMethods()) {
			if (method.isSynthetic()) {
				names.add(method.getName());
			}
		}

		assertTrue(names.contains("$deserializeLambda$"));
	}

}
