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
package org.jacoco.core.internal.analysis.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Unit tests for {@link KotlinSafeCallOperatorFilter}.
 */
public class KotlinSafeCallOperatorFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinSafeCallOperatorFilter();

	/**
	 * <pre>
	 * data class A(val b: B)
	 * data class B(val c: String)
	 * fun example(a: A?): String? =
	 *     a?.b?.c
	 * </pre>
	 *
	 * https://github.com/JetBrains/kotlin/commit/0a67ab54fec635f82e0507cbdd4299ae0dbe71b0
	 */
	@Test
	public void should_filter_optimized_safe_call_chain() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LA;)Ljava/lang/String;", null, null);
		m.visitVarInsn(ALOAD, 1);
		final Label label1 = new Label();
		final Label label2 = new Label();

		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(IFNULL, label1);
		final AbstractInsnNode i1 = m.instructions.getLast();
		m.visitMethodInsn(INVOKEVIRTUAL, "A", "getB", "()LB;", false);

		m.visitInsn(Opcodes.DUP);
		m.visitJumpInsn(IFNULL, label1);
		final AbstractInsnNode i2 = m.instructions.getLast();
		m.visitMethodInsn(INVOKEVIRTUAL, "B", "getC", "()Ljava/lang/String;",
				false);
		final HashSet<AbstractInsnNode> r = new HashSet<AbstractInsnNode>();
		r.add(m.instructions.getLast());

		m.visitJumpInsn(GOTO, label2);

		m.visitLabel(label1);
		m.visitInsn(Opcodes.POP);
		r.add(m.instructions.getLast());
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLabel(label2);

		filter.filter(m, context, output);

		assertIgnored();
		final HashMap<AbstractInsnNode, Set<AbstractInsnNode>> expected = new HashMap<AbstractInsnNode, Set<AbstractInsnNode>>();
		expected.put(i1, r);
		expected.put(i2, r);
		assertReplacedBranches(expected);
	}

	/**
	 * <pre>
	 * data class A(val b: B)
	 * data class B(val c: String)
	 * fun example(a: A?): String? =
	 *     a
	 *         ?.b
	 *         ?.c
	 * </pre>
	 */
	@Test
	public void should_filter_unoptimized_safe_call_chain() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LA;)Ljava/lang/String;", null, null);
		m.visitVarInsn(ALOAD, 0);
		final Label label1 = new Label();
		final Label label2 = new Label();

		m.visitJumpInsn(IFNULL, label1);
		final AbstractInsnNode i1 = m.instructions.getLast();
		m.visitVarInsn(ALOAD, 0);
		m.visitMethodInsn(INVOKEVIRTUAL, "A", "getB", "()LB;", false);

		m.visitVarInsn(ASTORE, 1);
		m.visitVarInsn(ALOAD, 1);
		m.visitJumpInsn(IFNULL, label1);
		final AbstractInsnNode i2 = m.instructions.getLast();
		m.visitVarInsn(ALOAD, 1);
		final HashSet<AbstractInsnNode> r = new HashSet<AbstractInsnNode>();
		r.add(m.instructions.getLast());
		m.visitMethodInsn(INVOKEVIRTUAL, "B", "getC", "()Ljava/lang/String;",
				false);

		m.visitJumpInsn(GOTO, label2);

		m.visitLabel(label1);
		m.visitInsn(Opcodes.ACONST_NULL);
		r.add(m.instructions.getLast());
		m.visitLabel(label2);

		filter.filter(m, context, output);

		assertIgnored();
		final HashMap<AbstractInsnNode, Set<AbstractInsnNode>> expected = new HashMap<AbstractInsnNode, Set<AbstractInsnNode>>();
		expected.put(i1, r);
		expected.put(i2, r);
		assertReplacedBranches(expected);
	}

	/**
	 * <pre>
	 * data class A()
	 * fun example(a: A?): String =
	 *     a
	 *         ?.c
	 *         ?: ""
	 * </pre>
	 *
	 * similar to
	 *
	 * <pre>
	 *         fun xx(a: B?, b: B?) {
	 *             if (a != null) {
	 *                 val t = b
	 *                 if (t != null) {
	 *                     nop(t)
	 *                 }
	 *             }
	 *             nop(a)
	 *         }
	 * </pre>
	 */
	@Test
	public void should_filter_unoptimized_safe_call_followed_by_elvis() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LA;)Ljava/lang/String;", null, null);
		m.visitCode();
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(81, label0);
		m.visitLineNumber(82, label0);
		m.visitVarInsn(ALOAD, 0);
		final Label label1 = new Label();
		m.visitJumpInsn(IFNULL, label1);
		final Label label2 = new Label();
		m.visitLabel(label2);
		m.visitLineNumber(81, label2);
		m.visitVarInsn(ALOAD, 0);
		Label label3 = new Label();
		m.visitLabel(label3);
		m.visitLineNumber(82, label3);
		m.visitMethodInsn(INVOKEVIRTUAL,
				"org/jacoco/core/test/validation/kotlin/targets/KotlinSafeCallOperatorTarget$B",
				"getC", "()Ljava/lang/String;", false);
		final Label label4 = new Label();
		m.visitLabel(label4);
		m.visitLineNumber(81, label4);
		m.visitVarInsn(ASTORE, 1);
		m.visitVarInsn(ALOAD, 1);
		m.visitJumpInsn(IFNULL, label1);
		m.visitVarInsn(ALOAD, 1);
		final Label label5 = new Label();
		m.visitJumpInsn(GOTO, label5);
		m.visitLabel(label1);
		m.visitLineNumber(83, label1);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitLdcInsn("");
		m.visitLabel(label5);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "java/lang/String" });
		m.visitInsn(ARETURN);
		final Label label6 = new Label();
		m.visitLabel(label6);
		m.visitLocalVariable("b",
				"Lorg/jacoco/core/test/validation/kotlin/targets/KotlinSafeCallOperatorTarget$B;",
				null, label0, label6, 0);
		m.visitMaxs(1, 2);
		m.visitEnd();

		filter.filter(m, context, output);

		// TODO
	}

}
