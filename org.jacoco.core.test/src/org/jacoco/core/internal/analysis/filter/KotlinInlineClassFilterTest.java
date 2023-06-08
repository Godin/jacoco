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
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link KotlinInlineClassFilter}.
 */
public class KotlinInlineClassFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinInlineClassFilter();

	/**
	 * <pre>
	 *   &#64;JvmInline
	 *   value class Example(val x: Int)
	 * </pre>
	 */
	@Test
	public void should_filter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"getX", "()I", null, null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.classAnnotations.add("Lkotlin/jvm/JvmInline;");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example", "unbox-impl", "()I",
				false);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 *   &#64;JvmInline
	 *   value class Example(val x: Int)
	 * </pre>
	 *
	 * after
	 * https://github.com/JetBrains/kotlin/commit/c4ddf3530d84c127d7757d7466f0e98a4a826c70
	 */
	@Test
	public void should_filter_Kotlin_1_5_20() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"getX", "()I", null, null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.classAnnotations.add("Lkotlin/jvm/JvmInline;");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "value", "I");
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 *   &#64;JvmInline
	 *   value class Example(val x: Int) {
	 *       fun getX() = x
	 *   }
	 * </pre>
	 */
	@Test
	public void should_not_filter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"getX-impl", "(I)I", null, null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		context.classAnnotations.add("Lkotlin/jvm/JvmInline;");

		m.visitVarInsn(Opcodes.ILOAD, 0);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
