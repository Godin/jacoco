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
package org.jacoco.core.test.validation.java14;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java14.targets.SwitchExpressionsTarget;

/**
 * Test of code coverage for switch expressions.
 */
public class SwitchExpressionsTest extends ValidationTestBase {

	public SwitchExpressionsTest() {
		super(SwitchExpressionsTarget.class);
	}

	@org.junit.Test
	public void snapshot() throws Exception {
		if (JavaVersion.current().isBefore("17")) {
			snapshotAllWithClassifier("javac_14_15_16");
		} else if (JavaVersion.current().isBefore("21")) {
			snapshotAllWithClassifier("javac_17_20");
		} else {
			snapshotAllWithClassifier(null);
		}
	}
}
