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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit test for {@link SMAP}.
 *
 * TODO use test cases from
 * {@link org.jacoco.core.internal.analysis.filter.KotlinInlineFilterTest}.
 */
public class SMAPTest {

	@Test
	public void should_parse() {
		final SMAP smap = new SMAP("Example.kt", "ExampleKt", "SMAP\n" //
				+ "Example.kt\n" // OutputFileName=Example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 Example.kt\n" // FileID=1,FileName=Example.kt
				+ "ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,3:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=3,OutputStartLine=1
				+ "1#1:4\n" // InputStartLine=1,LineFileID=1,OutputStartLine=4
				+ "*E\n");
		assertEquals(1, smap.getIntervals().size());
		final SMAP.Interval interval = smap.getIntervals().get(0);
		assertEquals("Example.kt", interval.sourceFile);
		assertEquals("ExampleKt", interval.className);
		assertEquals(1, interval.inputStartLine);
		assertEquals(1, interval.repeatCount);
		assertEquals(4, interval.outputStartLine);
	}

	@Test
	public void should_parse_without_KotlinDebug_stratum() {
		final SMAP smap = new SMAP("Example.kt", "ExampleKt", "SMAP\n" //
				+ "Example.kt\n" // OutputFileName=Example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 Example.kt\n" // FileID=1,FileName=Example.kt
				+ "ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,3:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=3,OutputStartLine=1
				+ "1#1:4\n" // InputStartLine=1,LineFileID=1,OutputStartLine=4
				+ "*S KotlinDebug\n"); // StratumID=KotlinDebug
		assertEquals(1, smap.getIntervals().size());
		final SMAP.Interval interval = smap.getIntervals().get(0);
		assertEquals("Example.kt", interval.sourceFile);
		assertEquals("ExampleKt", interval.className);
		assertEquals(1, interval.inputStartLine);
		assertEquals(1, interval.repeatCount);
		assertEquals(4, interval.outputStartLine);
	}

	// TODO better name
	@Test
	public void should_throw_exception_when_SMAP_incomplete() {
		try {
			new SMAP("", "", "SMAP\n");
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: null", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_DefaultStratumId_is_not_Kotlin() {
		try {
			new SMAP("Servlet.java", "", "SMAP\n" //
					+ "Servlet.java\n" // OutputFileName=Servlet.java
					+ "JSP\n"); // DefaultStratumId=JSP
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: JSP", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_first_StratumId_is_not_Kotlin() {
		try {
			new SMAP("Example.kt", "", "SMAP\n" //
					+ "Example.kt\n" // OutputFileName=Example.kt
					+ "Kotlin\n" // DefaultStratumId=Kotlin
					+ "*S KotlinDebug\n"); // StratumID=KotlinDebug
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: *S KotlinDebug",
					e.getMessage());
		}
	}

	// TODO StratumID not followed by FileSection

	@Test
	public void should_throw_exception_when_FileSection_ends_improperly() {
		try {
			new SMAP("callsite.kt", "", "SMAP\n" //
					+ "callsite.kt\n" //
					+ "Kotlin\n" //
					+ "*S Kotlin\n" //
					+ "*F\n" //
			);
			fail("exception expected");
		} catch (final NullPointerException e) {
			// expected
		}
	}

	@Test
	public void should_throw_exception_when_FileSection_contains_unexpected_FileInfo() {
		try {
			new SMAP("callsite.kt", "", "SMAP\n" //
					+ "callsite.kt\n" //
					+ "Kotlin\n" //
					+ "*S Kotlin\n" //
					+ "*F\n" //
					+ "xxx");
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_FileSection_is_empty() {
		try {
			new SMAP("Example.kt", "", "SMAP\n" //
					+ "Example.kt\n" // OutputFileName=Example.kt
					+ "Kotlin\n" // DefaultStratumId=Kotlin
					+ "*S Kotlin\n" // StratumID=Kotlin
					+ "*F\n" // FileSection
					+ "*L\n" // LineSection
			);
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP FileSection", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_LineSection_contains_unexpected_LineInfo() {
		try {
			new SMAP("callsite.kt", "", "SMAP\n" //
					+ "callsite.kt\n" //
					+ "Kotlin\n" //
					+ "*S Kotlin\n" //
					+ "*F\n" //
					+ "+ 1 callsite.kt\n" //
					+ "Callsite\n" //
					+ "*L\n" //
					+ "xxx");
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: xxx", e.getMessage());
		}
	}

	@Test
	public void should_throw_exception_when_LineSection_ends_improperly() {
		try {
			new SMAP("Example.kt", "", "SMAP\n" //
					+ "Example.kt\n" // OutputFileName=Example.kt
					+ "Kotlin\n" // DefaultStratumId=Kotlin
					+ "*S Kotlin\n" // StratumID=Kotlin
					+ "*F\n" // FileSection
					+ "+ 1 Example.kt\n" // FileID=1,FileName=Example.kt
					+ "ExampleKt\n" //
					+ "*L\n" // LineSection
					+ "1#1,3:1\n"); // InputStartLine=1,LineFileID=1,RepeatCount=3,OutputStartLine=1
			fail("exception expected");
		} catch (final NullPointerException e) {
			// expected
		}
	}

}
