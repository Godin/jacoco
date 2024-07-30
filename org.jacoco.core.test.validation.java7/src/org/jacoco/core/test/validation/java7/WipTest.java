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
package org.jacoco.core.test.validation.java7;

import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java7.targets.WipTarget;
import org.junit.AssumptionViolatedException;

/**
 * https://groups.google.com/g/jacoco/c/s2clBCBXzac/m/GIfNN4R8AQAJ
 */
public class WipTest extends ValidationTestBase {

	public WipTest() {
		super(WipTarget.class);
		if (!isJDKCompiler || JavaVersion.current().isBefore("11")) {
			throw new AssumptionViolatedException("");
		}
	}

}
