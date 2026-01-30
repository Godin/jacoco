/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java8;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java8.targets.MethodReferenceTarget;

/**
 * Test of code coverage in {@link MethodReferenceTarget}.
 */
public class MethodReferenceTest extends ValidationTestBase {

	public MethodReferenceTest() {
		super(MethodReferenceTarget.class);
	}

	/**
	 * Seems to be related to https://openjdk.org/jeps/371
	 */
	public void assertP(final Source.Line line) {
		if (isJDKCompiler && JavaVersion.current().isBefore("15")) {
			assertPartlyCovered(line);
		} else {
			assertFullyCovered(line);
		}
	}

	/**
	 * ECJ generates LINENUMBER 1
	 */
	public void assertPartlyCoveredECJ(final Source.Line line) {
		if (isJDKCompiler) {
			assertPartlyCovered(line);
		} else {
			assertFullyCovered(line);
		}
	}

	@Override
	public void first_line_in_coverage_data_should_be_greater_than_one() {
		if (isJDKCompiler) {
			super.first_line_in_coverage_data_should_be_greater_than_one();
		}
	}

}
