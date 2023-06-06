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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;

/**
 * Unit tests for {@link KotlinWipFilter}.
 */
public class KotlinWipFilterTest extends FilterTestBase {

	private final KotlinWipFilter filter = new KotlinWipFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"example", "()V", null, null);

	/**
	 * Kotlin 1.8.20
	 *
	 * <pre>
	 * data class Item(val data: String)
	 * data class Container(val item: Item)
	 * fun example(c: Container?): String {
	 * 	 return c?.item?.data + c?.item?.data
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_0() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder",
				"<init>", "()V", false);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		Label label1 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode i1 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Container", "getItem",
				"()LItem;", false);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode i2 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Item", "getData",
				"()Ljava/lang/String;", false);
		Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);
		m.visitLabel(label1);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLabel(label2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
				"append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				false);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		Label label3 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, label3);
		final AbstractInsnNode i3 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Container", "getItem",
				"()LItem;", false);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label3);
		final AbstractInsnNode i4 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Item", "getData",
				"()Ljava/lang/String;", false);
		Label label4 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label4);
		m.visitLabel(label3);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLabel(label4);
		m.visitFrame(Opcodes.F_FULL, 1, new Object[] { "Container" }, 2,
				new Object[] { "java/lang/StringBuilder", "java/lang/String" });
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
				"append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				false);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
				"toString", "()Ljava/lang/String;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		final HashMap<AbstractInsnNode, AbstractInsnNode> merge = new HashMap<AbstractInsnNode, AbstractInsnNode>();
		merge.put(i1, i2);
		merge.put(i3, i4);
		assertMerged(merge);
	}

	/**
	 * Kotlin 1.8.20
	 *
	 * <pre>
	 * data class Item(val data: String)
	 * data class Container(val item: Item)
	 * fun example(c: Container?): String? {
	 *   return c?.item?.data
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_1() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final Label label1 = new Label();
		final Label label2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode i1 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Container", "getItem",
				"LItem;", false);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode i2 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Item", "getData",
				"Ljava/lang/String;", false);
		m.visitJumpInsn(Opcodes.GOTO, label2);
		m.visitLabel(label1);
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLabel(label2);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
		assertMerged(i1, i2);
	}

	/**
	 * Kotlin 1.8.20
	 *
	 * <pre>
	 * data class Item(val data: String)
	 * data class Container(val item: Item)
	 * fun example(c: Container?): String {
	 *   return c?.item?.data ?: ""
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_2() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final Label label1 = new Label();
		final Label label2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode i1 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Container", "getItem",
				"LItem;", false);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode i2 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Item", "getData",
				"Ljava/lang/String;", false);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label2);
		m.visitLabel(label1);
		m.visitInsn(Opcodes.POP);
		m.visitLdcInsn("");
		m.visitLabel(label2);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
		assertMerged(i1, i2);
	}

}
