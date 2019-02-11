/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.BitSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DiffTest {

	@Test
	public void changed_file() throws Exception {
		BufferedReader r = new BufferedReader(new StringReader("" //
                + "diff --git a/file b/file\n" //
                + "index 1111111..1111111 100644\n"//
				+ "--- a/file\n" //
				+ "+++ b/file\n" //
				+ "@@ -1,3 +1,2 @@\n" //
				+ "-\n" // removed
				+ " \n" // unchanged
				+ "-\n" // removed
				+ "+\n" // added
		));
		Map<String, BitSet> diff = Diff.read(r);
		assertEquals("{2}", diff.get("b/file").toString());
		assertEquals(1, diff.size());
	}

	@Test
	public void added_file() throws Exception {
		BufferedReader r = new BufferedReader(new StringReader("" //
				+ "diff --git a/file b/file\n" //
				+ "new file mode 100644\n" //
				+ "index 0000000..1111111\n" //
				+ "--- /dev/null\n" //
				+ "+++ b/file\n" //
				+ "@@ -0,0 +1 @@\n" //
				+ "+\n" // added line
		));
		Map<String, BitSet> diff = Diff.read(r);
		assertEquals("{1}", diff.get("b/file").toString());
		assertEquals(1, diff.size());
	}

	@Test
	public void deleted_file() throws Exception {
		BufferedReader r = new BufferedReader(new StringReader("" //
				+ "diff --git a/file b/file\n" //
				+ "deleted file mode 100644\n" //
				+ "index 1111111..0000000\n" //
				+ "--- a/file\n" //
				+ "+++ /dev/null\n" //
				+ "@@ -1,1 +0,0 @@\n" //
				+ "-\n" // removed line
		));
		Map<String, BitSet> diff = Diff.read(r);
		assertEquals("{}", diff.get("/dev/null").toString());
		assertEquals(1, diff.size());
	}

}
