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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MemoryOutput;
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

	private MemoryOutput output;

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
		output = new MemoryOutput();
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
				"Package: org/jacoco/example", //
				"  Instructions: 1/1", //
				"  Branches: 1/1", //
				"Source file: org/jacoco/example/FooClass.java", //
				"  Instructions: 1/1", //
				"  Branches: 1/1", //
				"       5/8     1 |class Example {", //
				" 2/3   5/8     2 |  void example() {", //
				"               3 |  }", //
				"       5/9     4 |}", //
				"Source file: org/jacoco/example/Empty.java", //
				"  Instructions: 1/1", //
				"  Branches: 1/1", //
				"               1 |package org.jacoco.example;", //
				"               2 |interface I {}", //
				"Package: empty", //
				"  Instructions: 1/1", //
				"  Branches: 1/1", //
				"Source file: empty/Empty.java", //
				"  Instructions: 1/1", //
				"  Branches: 1/1", //
				"               1 |package empty;", //
				"               2 |interface I {}"), //
				output.toString());
		output.assertClosed();
	}

	@Test
	public void visitGroup_should_throw_UnsupportedOperationException()
			throws IOException {
		final IReportVisitor visitor = formatter
				.createVisitor(new ByteArrayOutputStream());
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
