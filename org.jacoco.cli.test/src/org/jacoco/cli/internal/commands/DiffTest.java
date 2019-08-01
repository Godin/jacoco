/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import org.jacoco.cli.internal.CommandTestBase;
import org.junit.Test;

/**
 * Unit tests for {@link Diff}.
 */
public class DiffTest extends CommandTestBase {

	@Test
	public void should_print_usage_when_no_options_are_given()
			throws Exception {
		execute("diff");
	}

	@Test
	public void wip() throws Exception {
		execute("diff", //
				"../diff-example/v1/jacoco.xml", //
				"../diff-example/v2/jacoco.xml", //
				"--sourcefiles", "../diff-example/src_v2", //
				"--html", "../diff-example/report"
		);
	}

}
