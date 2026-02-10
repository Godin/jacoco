/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java21;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java21.targets.RecordPatternsTarget;

/**
 * Test of code coverage in {@link RecordPatternsTarget}.
 */
public class RecordPatternsTest extends ValidationTestBase {

	public RecordPatternsTest() {
		super(RecordPatternsTarget.class);
	}

	public void assertInstanceof(final Line line) {
		if (JavaVersion.current().isBefore("23") || !isJDKCompiler) {
			assertFullyCovered(line, 0, 2);
		} else {
			// TODO https://bugs.openjdk.org/browse/JDK-8303374
			assertFullyCovered(line, 2, 4);
		}
	}

	public void assertSwitchStatementLastCase(final Line line) {
		if (!isJDKCompiler) {
			// TODO unfortunately
			// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
			// did not fixed this issue
			assertPartlyCovered(line);
		} else if (JavaVersion.current().isBefore("23")) {
			assertFullyCovered(line);
		} else {
			// TODO https://bugs.openjdk.org/browse/JDK-8303374
			assertPartlyCovered(line, 2, 2);
		}
	}

	public void fullyJavac(final Line line) {
		if (isJDKCompiler) {
			assertFullyCovered(line, 0, 0);
		}
	}

	public void fullyEcj(final Line line, final int missedBranches,
			final int coveredBranches) {
		if (!isJDKCompiler) {
			assertFullyCovered(line, missedBranches, coveredBranches);
		}
	}

	public void partlyEcj(final Line line, final int missedBranches,
			final int coveredBranches) {
		if (!isJDKCompiler) {
			assertPartlyCovered(line, missedBranches, coveredBranches);
		}
	}

	@org.junit.Test
	public void testBytecodes() throws Exception {
		assertBytecode(RecordPatternsTarget.class, "switchStatement1");
		assertBytecode(RecordPatternsTarget.class, "outerLookupSwitch");
		assertBytecode(RecordPatternsTarget.class, "innerInstanceof");
		assertBytecode(RecordPatternsTarget.class, "exhaustive");
	}

}
