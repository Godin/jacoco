/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.report.internal.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

import org.jacoco.core.internal.InputStreams;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportStructureTestDriver;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link TextFormatter}.
 */
public class TextFormatterTest {

	private TextFormatter formatter;

	private HashMap<String, String> sources;

	private ReportStructureTestDriver driver;

	private MemoryMultiReportOutput output;

	@Before
	public void setup() {
		formatter = new TextFormatter();
		sources = new HashMap<String, String>();
		driver = new ReportStructureTestDriver(new ISourceFileLocator() {
			public Reader getSourceFile(final String packageName,
					final String fileName) {
				final String source = sources.get(packageName + "/" + fileName);
				return source == null ? null : new StringReader(source);
			}

			public int getTabWidth() {
				return 4;
			}
		});
		output = new MemoryMultiReportOutput();
	}

	@Test
	public void test() throws IOException {
		sources.put("org/jacoco/example/FooClass.java", lines( //
				"class Example {", //
				"  void example() {", //
				"  }", //
				"}"));
		sources.put("org/jacoco/example/Empty.java", lines(//
				"package org.jacoco.example;", //
				"interface I {}"));
		sources.put("empty/Empty.java", lines( //
				"package empty;", //
				"interface I {}"));

		final IReportVisitor visitor = formatter.createVisitor(output);
		driver.sendBundle(visitor);

		// TODO test source file not found
		assertEquals(lines( //
				"       5/8  |class Example {", //
				" 2/3   5/8  |  void example() {", //
				"            |  }", //
				"       5/9  |}", //
				"Total:", //
				"  instructions: 15/25", //
				"  branches: 2/3", //
				"  lines: 3/3"), //
				getFile(output, "org/jacoco/example/FooClass.java.txt"));
		assertEquals(lines( //
				"            |package org.jacoco.example;", //
				"            |interface I {}", //
				"Total:", //
				"  instructions: 0/0", //
				"  branches: 0/0", //
				"  lines: 0/0"), //
				getFile(output, "org/jacoco/example/Empty.java.txt"));
		assertEquals(lines( //
				"            |package empty;", //
				"            |interface I {}", //
				"Total:", //
				"  instructions: 0/0", //
				"  branches: 0/0", //
				"  lines: 0/0"), //
				getFile(output, "empty/Empty.java.txt"));

		output.assertAllClosed();
	}

	private static String getFile(final MemoryMultiReportOutput output,
			final String path) throws IOException {
		return new String(InputStreams.readFully(output.getFileAsStream(path)));
	}

	@Test
	public void visitGroup_should_throw_UnsupportedOperationException()
			throws IOException {
		final IReportVisitor visitor = formatter.createVisitor(output);
		try {
			visitor.visitGroup("group");
			fail("exception expected");
		} catch (final UnsupportedOperationException e) {
			// expected
		}
	}

	private static String lines(String... lines) {
		final StringBuilder sb = new StringBuilder();
		for (final String line : lines) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

}
