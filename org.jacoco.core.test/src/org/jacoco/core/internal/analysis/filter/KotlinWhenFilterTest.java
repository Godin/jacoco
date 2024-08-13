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

import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinWhenFilter}.
 */
public class KotlinWhenFilterTest extends FilterTestBase {

	private final KotlinWhenFilter filter = new KotlinWhenFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	@Test
	public void should_filter_implicit_else() {
		final Label label = new Label();

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFEQ, label);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		final Range range2 = new Range();
		m.visitLabel(label);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

	@Test
	public void should_not_filter_explicit_else() {
		final Label label = new Label();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFEQ, label);

		m.visitInsn(Opcodes.NOP);

		m.visitLabel(label);
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Throwable");
		m.visitInsn(Opcodes.ATHROW);

		filter.filter(m, context, output);

		assertIgnored();
		assertNoReplacedBranches();
	}

	@Test
	public void should_filter_implicit_default() {
		final Label case1 = new Label();
		final Label caseDefault = new Label();
		final Label after = new Label();

		m.visitInsn(Opcodes.NOP);

		m.visitTableSwitchInsn(0, 0, caseDefault, case1);
		final AbstractInsnNode switchNode = m.instructions.getLast();
		final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();

		m.visitLabel(case1);
		m.visitInsn(Opcodes.ICONST_1);
		newTargets.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, after);

		final Range range1 = new Range();
		m.visitLabel(caseDefault);
		range1.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();

		m.visitLabel(after);

		filter.filter(m, context, output);

		assertIgnored(range1);
		assertReplacedBranches(switchNode, newTargets);
	}

	/**
	 * <pre>
	 * enum class E {
	 *   A
	 * }
	 *
	 * fun example(e: E?): Int = when (e) {
	 *   null -> 0
	 *   E.A -> 1
	 * }
	 * </pre>
	 */
	@org.junit.Ignore
	@Test
	public void should_filter_wip() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(LE;)I", null, null);
		m.visitCode();
		Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(5, label0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		Label label1 = new Label();
		m.visitJumpInsn(Opcodes.IFNONNULL, label1);
		final Range range0 = new Range();
		range0.fromInclusive = m.instructions.getLast();
		range0.toInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ICONST_M1);
		Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);
		m.visitLabel(label1);
		m.visitFieldInsn(Opcodes.GETSTATIC, "ExampleKt$WhenMappings",
				"$EnumSwitchMapping$0", "[I");
		m.visitInsn(Opcodes.SWAP);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "E", "ordinal", "()I", false);
		m.visitInsn(Opcodes.IALOAD);
		m.visitLabel(label2);
		Label label3 = new Label();
		Label label4 = new Label();
		Label label5 = new Label();
		m.visitTableSwitchInsn(-1, 1, label4,
				new Label[] { label3, label4, label5 });
		m.visitLabel(label3);
		m.visitLineNumber(6, label3);
		m.visitInsn(Opcodes.ICONST_0);
		Label label6 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label6);
		m.visitLabel(label5);
		m.visitLineNumber(7, label5);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitJumpInsn(Opcodes.GOTO, label6);
		m.visitLabel(label4);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(5, label4);
		m.visitTypeInsn(Opcodes.NEW, "kotlin/NoWhenBranchMatchedException");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"kotlin/NoWhenBranchMatchedException", "<init>", "()V", false);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();
		m.visitLabel(label6);
		m.visitLineNumber(8, label6);
		m.visitInsn(Opcodes.IRETURN);
		Label label7 = new Label();
		m.visitLabel(label7);
		m.visitLocalVariable("e", "LE;", null, label0, label7, 0);
		m.visitMaxs(2, 1);
		m.visitEnd();

		filter.filter(m, context, output);

		assertIgnored(range0, range1);
	}

}
