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
				"--html", "../diff-example/report");
	}

	@Test
	public void jacoco() throws Exception {
		String[] versions = new String[] { //
				"v0.8.4", //
				"v0.8.3", //
				"v0.8.2", //
				"v0.8.1", //
				"v0.8.0", //
				"v0.7.9", //
				"v0.7.8", //
				"v0.7.7", //
		};
		for (int i = 0; i < versions.length - 1; i++) {
			String from = versions[i + 1];
			String to = versions[i];
			execute("diff", //
					"../../jacoco-diff/reports/" + from + "/jacoco.xml", //
					"../../jacoco-diff/reports/" + to + "/jacoco.xml", //
					"--sourcefiles", "../diff-example/src_v2", //
					"--html", "../../jacoco-diff/diff/" + to, //
					"--name", "Diff from " + from + " to " + to);
		}
	}

}
