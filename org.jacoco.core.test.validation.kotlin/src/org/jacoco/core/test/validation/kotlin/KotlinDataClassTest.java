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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinDataClassTargetKt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KotlinDataClassTest extends ValidationTestBase {

	private Set<String> expectedMethods = new HashSet<String>();

	public KotlinDataClassTest() {
		super(KotlinDataClassTargetKt.class);
	}

	@Test
	@Override
	public void execute_assertions_in_comments() throws IOException {
		super.execute_assertions_in_comments();
		assertEquals(expectedMethods, methods);
	}

	public void method(Source.Line line, String name) {
		this.expectedMethods.add(name);
	}

}
