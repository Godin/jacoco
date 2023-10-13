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
package org.jacoco.core.internal.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO reuse in
 * {@link org.jacoco.core.internal.analysis.filter.KotlinInlineFilter}
 */
public final class SMAP {

	static final boolean DEBUG = false;

	public static final class Interval {
		public final String className;
		final String sourceFile;
		public final int inputStartLine;
		final int repeatCount;
		public final int outputStartLine;

		private Interval(final String className, final String sourceFile,
				final int inputStartLine, final int repeatCount,
				final int outputStartLine) {
			this.className = className;
			this.sourceFile = sourceFile;
			this.inputStartLine = inputStartLine;
			this.repeatCount = repeatCount;
			this.outputStartLine = outputStartLine;
		}
	}

	private final List<Interval> intervals = new ArrayList<Interval>();

	public List<Interval> getIntervals() {
		return intervals;
	}

	public SMAP(final String sourceFileName, final String className, final String smap) {
		try {
			final BufferedReader br = new BufferedReader(
					new StringReader(smap));
			expectLine(br, "SMAP");
			// OutputFileName
			expectLine(br, sourceFileName);
			// DefaultStratumId
			expectLine(br, "Kotlin");
			// StratumSection
			expectLine(br, "*S Kotlin");
			// FileSection
			expectLine(br, "*F");
			final HashMap<Integer, String> sourceFiles = new HashMap<Integer, String>();
			final HashMap<Integer, String> classNames = new HashMap<Integer, String>();
			String line;
			while (!"*L".equals(line = br.readLine())) {
				// https://github.com/JetBrains/kotlin/blob/1.9.0/compiler/backend/src/org/jetbrains/kotlin/codegen/SourceInfo.kt#L41
				final String absoluteFileName = br.readLine();
				final Matcher m = FILE_INFO_PATTERN.matcher(line);
				if (!m.matches()) {
					throw new IllegalStateException(
							"Unexpected SMAP line: " + line);
				}
				final int id = Integer.parseInt(m.group(1));
				final String fileName = m.group(2);
				sourceFiles.put(id, fileName);
				classNames.put(id, absoluteFileName);
			}
			if (sourceFiles.isEmpty()) {
				throw new IllegalStateException("Unexpected SMAP FileSection");
			}
			// LineSection
			while (true) {
				line = br.readLine();
				if (line.equals("*E") || line.equals("*S KotlinDebug")) {
					break;
				}
				final Matcher m = LINE_INFO_PATTERN.matcher(line);
				if (!m.matches()) {
					throw new IllegalStateException(
							"Unexpected SMAP line: " + line);
				}
				final int inputStartLine = Integer.parseInt(m.group(1));
				final int lineFileID = Integer
						.parseInt(m.group(2).substring(1));
				final String repeatCountOptional = m.group(3);
				final int repeatCount = repeatCountOptional != null
						? Integer.parseInt(m.group(3).substring(1))
						: 1;
				final int outputStartLine = Integer.parseInt(m.group(4));
				final String intervalClassName = classNames.get(lineFileID);
				if (intervalClassName.equals(className)
						&& outputStartLine == 1) {
					continue;
				}
				intervals.add(new Interval(classNames.get(lineFileID),
						sourceFiles.get(lineFileID), inputStartLine,
						repeatCount, outputStartLine));
			}
		} catch (final IOException e) {
			// Must not happen with StringReader
			throw new AssertionError(e);
		}
	}

	private static void expectLine(final BufferedReader br,
			final String expected) throws IOException {
		final String line = br.readLine();
		if (!expected.equals(line)) {
			throw new IllegalStateException("Unexpected SMAP line: " + line);
		}
	}

	private static final Pattern LINE_INFO_PATTERN = Pattern.compile("" //
			+ "([0-9]++)" // InputStartLine
			+ "(#[0-9]++)?+" // LineFileID
			+ "(,[0-9]++)?+" // RepeatCount
			+ ":([0-9]++)" // OutputStartLine
			+ "(,[0-9]++)?+" // OutputLineIncrement
	);

	private static final Pattern FILE_INFO_PATTERN = Pattern.compile("" //
			+ "\\+ ([0-9]++)" // FileID
			+ " (.++)" // FileName
	);

}
