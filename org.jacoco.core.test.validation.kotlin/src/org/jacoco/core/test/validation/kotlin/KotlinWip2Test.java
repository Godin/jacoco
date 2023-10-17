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

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinInline;
import org.jacoco.core.test.validation.kotlin.targets.KotlinWipTarget;

/**
 * Test of code coverage in {@link KotlinWipTarget}.
 */
public class KotlinWip2Test extends ValidationTestBase {

	public KotlinWip2Test() {
		super(KotlinInline.class);
	}

	@Override
	public void all_missed_instructions_should_have_line_number() {
		// instructions without line number in inline function
		// FIXME they show up in reports
	}

	@Override
	public void all_branches_should_have_line_number() {
		super.all_branches_should_have_line_number();
	}

}
