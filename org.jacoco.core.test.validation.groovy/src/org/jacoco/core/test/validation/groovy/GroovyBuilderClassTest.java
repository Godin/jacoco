/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jan Wloka - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.groovy;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovyBuilderClassTarget;
import org.junit.Test;

/**
 * Test of class with {@link groovy.transform.builder.Builder} annotation.
 */
public class GroovyBuilderClassTest extends ValidationTestBase {
	public GroovyBuilderClassTest() {
		super(GroovyBuilderClassTarget.class);
	}

	@Test
	public void test_method_count() {
		assertMethodCount(1);
	}
}
