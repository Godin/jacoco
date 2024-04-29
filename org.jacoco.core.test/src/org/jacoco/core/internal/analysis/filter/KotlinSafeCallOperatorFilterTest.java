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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link KotlinSafeCallOperatorFilter}.
 */
public class KotlinSafeCallOperatorFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinSafeCallOperatorFilter();

	/**
	 * <pre>
	 * data class A(val b: B)
	 * data class B(val c: String)
	 * fun example(a: A?): String? {
	 *     return a?.b?.c
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		final Label label1 = new Label();
		final Label label2 = new Label();
		final HashSet<AbstractInsnNode> r = new HashSet<AbstractInsnNode>();
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
		r.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, label2);
		m.visitLabel(label1);
		m.visitInsn(Opcodes.POP);
		r.add(m.instructions.getLast());
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLabel(label2);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		final HashMap<AbstractInsnNode, HashSet<AbstractInsnNode>> expected = new HashMap<AbstractInsnNode, HashSet<AbstractInsnNode>>();
		expected.put(i1, r);
		expected.put(i2, r);
		assertEquals(expected, replacedBranches);
	}

	/**
	 * <pre>
	 * data class A(val b: B)
	 * data class B(val c: String)
	 * fun example(a: A?): String {
	 *     return a?.b?.c ?: ""
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_2() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		final Label label1 = new Label();
		final Label label2 = new Label();
		final HashSet<AbstractInsnNode> r = new HashSet<AbstractInsnNode>();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1);
		final AbstractInsnNode i1 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Container", "getItem",
				"LItem;", false);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNULL, label1); // TODO can't be null
		final AbstractInsnNode i2 = m.instructions.getLast();
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Item", "getData",
				"Ljava/lang/String;", false);
		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(Opcodes.IFNONNULL, label2); // TODO can't be null
		final AbstractInsnNode i3 = m.instructions.getLast();
		m.visitLabel(label1);
		m.visitInsn(Opcodes.POP);
		r.add(m.instructions.getLast());
		m.visitLdcInsn("");
		m.visitLabel(label2);
		m.visitInsn(Opcodes.ARETURN);
		r.add(m.instructions.getLast());

		filter.filter(m, context, output);

		final HashMap<AbstractInsnNode, HashSet<AbstractInsnNode>> expected = new HashMap<AbstractInsnNode, HashSet<AbstractInsnNode>>();
		expected.put(i1, r);
		expected.put(i2, r);
		expected.put(i3, r);
		assertEquals(expected, replacedBranches);
	}

}
