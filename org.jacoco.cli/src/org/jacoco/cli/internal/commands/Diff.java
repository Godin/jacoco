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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Diff {

	private static final Pattern HUNK_HEADER_PATTERN = Pattern.compile(
			"@@ -([0-9]++)(,([0-9]++))?\\s\\+([0-9]++)(,([0-9]++))?\\s@@.*+");

	private Diff() {
	}

	static Map<String, BitSet> read(final BufferedReader r) throws IOException {
		final Map<String, BitSet> result = new HashMap<String, BitSet>();
		String s = r.readLine();
		while (s != null) {
			// read header
			if (!s.startsWith("--- ")) {
				s = r.readLine();
				continue;
			}
			s = r.readLine();
			if (!s.startsWith("+++ ")) {
				throw new IOException("Expected new filename, got: " + s);
			}
			final String filename = s.substring("+++ ".length());
			final BitSet lines = new BitSet();
			result.put(filename, lines);
			// read hunks
			s = r.readLine();
			do {
				readHunk(r, lines, s);
				s = r.readLine();
			} while (s != null && s.startsWith("@@ "));
		}
		return result;
	}

	private static void readHunk(final BufferedReader r, final BitSet lines,
			final String s) throws IOException {
		final Matcher m = HUNK_HEADER_PATTERN.matcher(s);
		if (!m.matches()) {
			throw new IOException("Expected hunk header, got: " + s);
		}
		final int oldOffset = Integer.parseInt(m.group(1));
		final int oldLength = parseOptionalInt(m.group(3));
		final int newOffset = Integer.parseInt(m.group(4));
		final int newLength = parseOptionalInt(m.group(6));
		readHunk(r, lines, oldOffset, oldLength, newOffset, newLength);
	}

	private static int parseOptionalInt(String s) {
		return s == null ? 1 : Integer.parseInt(s);
	}

	private static void readHunk(final BufferedReader r, final BitSet lines,
			int oldOffset, int oldLength, int newOffset, int newLength)
			throws IOException {
		while (oldLength > 0 || newLength > 0) {
			final String line = r.readLine();
			if (line.startsWith("-")) {
				oldOffset++;
				oldLength--;
			} else if (line.startsWith("+")) {
				lines.set(newOffset);
				newOffset++;
				newLength--;
			} else if (line.startsWith(" ")) {
				oldOffset++;
				oldLength--;
				newOffset++;
				newLength--;
			}
		}
	}

}
