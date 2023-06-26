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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.Source;
import org.jacoco.core.test.validation.kotlin.targets.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class KotlinVersionsTest extends KotlinValidationTestBase {

	@Parameterized.Parameters(name = "Kotlin {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				// {"1.4.0"},
				// Up to latest in 1.5.x
				{ "1.5.0" }, //
				{ "1.5.30" }, //
				{ "1.5.32" }, //
				// Up to latest in 1.6.x
				{ "1.6.0" }, //
				{ "1.6.10" }, //
				{ "1.6.20" }, //
				{ "1.6.21" }, //
				// Up to latest in 1.7.x
				{ "1.7.0" }, //
				{ "1.7.22" },
				// Up to latest in 1.8.x
				{ "1.8.0" }, //
				{ "1.8.22" }, //
				{ "1.9.0-RC" }, //
		});
	}

	@SuppressWarnings("unused")
	public void assertCovered(final Source.Line line,
			final String expectedBefore, final int messedBranchesBefore,
			final int coveredBranchesBefore, final String kotlinVersion,
			final String expected, final int missedBranches,
			final int coveredBranches) {
		if (this.kotlinVersion.isBefore(kotlinVersion)) {
			assertCoverage(line, statusByName(expectedBefore),
					messedBranchesBefore, coveredBranchesBefore);
		} else {
			assertCoverage(line, statusByName(expected), missedBranches,
					coveredBranches);
		}
	}

	private int statusByName(String name) {
		if ("PARTLY".equals(name)) {
			return ICounter.PARTLY_COVERED;
		} else if ("FULLY".equals(name)) {
			return ICounter.FULLY_COVERED;
		} else if ("NOT".equals(name)) {
			return ICounter.NOT_COVERED;
		} else if ("EMPTY".equals(name)) {
			return ICounter.EMPTY;
		} else {
			throw new IllegalArgumentException(name);
		}
	}

	public KotlinVersionsTest(final String kotlinVersion) throws Exception {
		// super(kotlinVersion, KotlinCallableReferenceTarget.class);
		// TODO diff in 1.4.0:
		// super(kotlinVersion, KotlinControlStructuresTarget.class);
		// super(kotlinVersion, KotlinCoroutineTarget.class); // OK
		// TODO diff in 1.4.0:
		// super(kotlinVersion, KotlinDataClassTarget.class);
		// super(kotlinVersion, KotlinDefaultArgumentsTarget.class); // OK
		// TODO fix filter in general:
		// super(kotlinVersion, KotlinDefaultMethodsTarget.class);
		// super(kotlinVersion, KotlinDelegatesTarget.class); // OK
		// super(kotlinVersion, KotlinElvisOperatorTarget.class); // OK
		// TODO missed instructions without line number in inline function:
		// super(kotlinVersion, KotlinInlineTargetKt.class);
		// super(kotlinVersion, KotlinLambdaExpressionsTarget.class); // OK
		// TODO fix filter for 1.5.30+
		// super(kotlinVersion, KotlinLateinitTarget.class);
		// super(kotlinVersion, KotlinNotNullOperatorTarget.class); // OK
		// super(kotlinVersion, KotlinSafeCallOperatorTarget.class); // OK
		// super(kotlinVersion, KotlinSafeCastTarget.class); // OK
		// super(kotlinVersion, KotlinTopLevelFunctionTargetKt.class); // OK
		// super(kotlinVersion, KotlinUnsafeCastOperatorTarget.class); // OK
		// TODO fails on 1.4.0, 1.6.20+
		super(kotlinVersion, KotlinWhenExpressionTarget.class);
	}

}
