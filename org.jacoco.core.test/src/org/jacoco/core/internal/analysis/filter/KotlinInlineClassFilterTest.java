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
package org.jacoco.core.internal.analysis.filter;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinInlineClassFilter}.
 */
public class KotlinInlineClassFilterTest extends FilterTestBase {

	private final KotlinInlineClassFilter filter = new KotlinInlineClassFilter();

	/**
	 * <pre>
	 * &#064;kotlin.jvm.JvmInline
	 * value class Example(val value: String) : Base {
	 *   override fun base() { ... }
	 * }
	 * </pre>
	 *
	 * <pre>
	 * class Example implements Base {
	 *   private final String value;
	 *   public String getValue() { return value; }
	 *
	 *   private synthetic Example(String) { this.value = value; }
	 *
	 *   public static String constructor-impl(String) { ... }
	 *
	 *   public void base() { base-impl(value); }
	 *   public static void base-impl(String) { ... }
	 *
	 *   public String toString() { return toString-impl(value); }
	 *   public static String toString-impl(String) { ... }
	 *
	 *   public boolean equals(Object) { return equals-impl(...); }
	 *   public static boolean equals-impl(String, Object) { ... }
	 *
	 *   public int hashCode() { return hashCode-impl(value); }
	 *   public static int hashCode-impl(String) { ... }
	 *
	 *   public static synthetic Example box-impl(String) { ... }
	 *   public final synthetic String unbox-impl() { ... }
	 *
	 *   public final static equals-impl0(String, String) { ... }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_non_static() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.classAnnotations.add("Lkotlin/jvm/JvmInline;");
		final MethodNode m = new MethodNode(0, "base", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_static() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.classAnnotations.add("Lkotlin/jvm/JvmInline;");
		final MethodNode m = new MethodNode(Opcodes.ACC_STATIC, "base-impl",
				"()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_no_JvmInline_annotation() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(0, "base", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_not_kotlin() {
		final MethodNode m = new MethodNode(0, "base", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
