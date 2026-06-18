/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.ArrayList;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link RecordPatternFilter}.
 */
public class RecordPatternFilterTest extends FilterTestBase {

	private final IFilter filter = new RecordPatternFilter();

	private final ArrayList<Replacement> replacements = new ArrayList<Replacement>();

	/**
	 * <pre>
	 *   record Point(int x, int y) {}
	 *
	 *   void example(Object o) {
	 *     if (o instanceof Point(int x, int y)) {
	 *       ...
	 *     }
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_instanceof() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);

		final Label start1 = new Label();
		final Label end1 = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start1, end1, handler, "java/lang/Throwable");
		final Label start2 = new Label();
		final Label end2 = new Label();
		m.visitTryCatchBlock(start2, end2, handler, "java/lang/Throwable");

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.INSTANCEOF, "Example$Point");
		final Label label1 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, label1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "Example$Point");
		m.visitVarInsn(Opcodes.ASTORE, 2);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLabel(start1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "x", "()I",
				false);
		m.visitLabel(end1);
		m.visitVarInsn(Opcodes.ISTORE, 5);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ISTORE, 3);

		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitLabel(start2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "y", "()I",
				false);
		m.visitLabel(end2);
		m.visitVarInsn(Opcodes.ISTORE, 5);
		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ISTORE, 4);

		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitVarInsn(Opcodes.ILOAD, 4);
		m.visitInsn(Opcodes.IADD);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "nop", "(I)V",
				false);

		m.visitLabel(label1);
		final Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);

		m.visitLabel(handler);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/MatchException");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"toString", "()Ljava/lang/String;", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
				"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(label2);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m, range, range);
	}

	/**
	 * <pre>
	 *   record Point(int x, int y) {}
	 *
	 *   void example(Object o) {
	 *     switch (o) {
	 *       case Point(int x, int y) -> ...
	 *       default -> ...
	 *     }
	 *   }
	 * </pre>
	 */
	@Test
	public void should_filter_switch() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);

		final Label start1 = new Label();
		final Label end1 = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start1, end1, handler, "java/lang/Throwable");
		final Label start2 = new Label();
		final Label end2 = new Label();
		m.visitTryCatchBlock(start2, end2, handler, "java/lang/Throwable");

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects",
				"requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;",
				false);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ILOAD, 3);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("LExample$Point;") });
		final Label case1 = new Label();
		final Label dflt = new Label();
		m.visitLookupSwitchInsn(dflt, new int[] { 0 }, new Label[] { case1 });
		m.visitLabel(case1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitTypeInsn(Opcodes.CHECKCAST, "Example$Point");
		m.visitVarInsn(Opcodes.ASTORE, 4);

		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitLabel(start1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "x", "()I",
				false);
		m.visitLabel(end1);
		m.visitVarInsn(Opcodes.ISTORE, 7);
		m.visitVarInsn(Opcodes.ILOAD, 7);
		m.visitVarInsn(Opcodes.ISTORE, 5);

		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitLabel(start2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example$Point", "y", "()I",
				false);
		m.visitLabel(end2);
		m.visitVarInsn(Opcodes.ISTORE, 7);
		m.visitVarInsn(Opcodes.ILOAD, 7);
		m.visitVarInsn(Opcodes.ISTORE, 6);

		m.visitVarInsn(Opcodes.ILOAD, 5);
		m.visitVarInsn(Opcodes.ILOAD, 6);
		m.visitInsn(Opcodes.IADD);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "nop", "(I)V",
				false);
		final Label label1 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label1);

		m.visitLabel(dflt);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example", "nop", "(I)V",
				false);
		m.visitLabel(label1);
		final Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);

		m.visitLabel(handler);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/MatchException");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"toString", "()Ljava/lang/String;", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
				"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(label2);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(m, range, range);
	}

	/**
	 * <pre>
	 * record R(Object component) {}
	 *
	 * void example(Object o) {
	 *   return switch (o) {
	 *     case R(String c) -> "R(String)";
	 *     case R(Integer c) -> "R(Integer)";
	 *     case R(R(String c)) -> "R(R(String)";
	 *     case R(R(Integer c)) -> "R(R(Integer))";
	 *     case String s -> "String";
	 *     case Integer i -> "Integer";
	 *     default -> ...
	 *   }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_nested() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "(Ljava/lang/Object;)Ljava/lang/String;", null,
				null);

		final Label labelComponent1Start = new Label();
		final Label labelComponent1End = new Label();
		final Label labelHandler = new Label();
		m.visitTryCatchBlock(labelComponent1Start, labelComponent1End,
				labelHandler, "java/lang/Throwable");
		final Label labelComponent2Start = new Label();
		final Label labelComponent2End = new Label();
		m.visitTryCatchBlock(labelComponent2Start, labelComponent2End,
				labelHandler, "java/lang/Throwable");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects",
				"requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;",
				false);
		m.visitInsn(Opcodes.POP);

		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		final Label labelOuter = new Label();
		m.visitLabel(labelOuter);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("LR;"),
						Type.getType("Ljava/lang/String;"),
						Type.getType("Ljava/lang/Integer;") });
		final Label labelCaseR = new Label();
		final Label labelCaseS = new Label();
		final Label labelCaseI = new Label();
		final Label labelCaseDefault = new Label();
		m.visitTableSwitchInsn(0, 2, labelCaseDefault,
				new Label[] { labelCaseR, labelCaseS, labelCaseI });
		final AbstractInsnNode outerSwitch = m.instructions.getLast();
		replacements.add(new Replacement(4, m.instructions.getLast(), 2));
		replacements.add(new Replacement(5, m.instructions.getLast(), 3));
		replacements.add(new Replacement(6, m.instructions.getLast(), 0));

		m.visitLabel(labelCaseR);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "R");
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitLabel(labelComponent1Start);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "R", "component",
				"()Ljava/lang/Object;", false);
		m.visitLabel(labelComponent1End);
		m.visitVarInsn(Opcodes.ASTORE, 9);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 10);
		final Label labelInner = new Label();
		m.visitLabel(labelInner);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitVarInsn(Opcodes.ILOAD, 10);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("Ljava/lang/String;"),
						Type.getType("Ljava/lang/Integer;"),
						Type.getType("LR;") });
		final Label labelOuterRestart = new Label();
		final Label labelCaseRS = new Label();
		final Label labelCaseRI = new Label();
		final Label labelCaseRR = new Label();
		m.visitTableSwitchInsn(-1, 2, labelOuterRestart, new Label[] {
				labelOuterRestart, labelCaseRS, labelCaseRI, labelCaseRR });
		final Range innerSwitch1 = new Range(m.instructions.getLast(),
				m.instructions.getLast());
		replacements.add(new Replacement(0, m.instructions.getLast(), 1));
		replacements.add(new Replacement(1, m.instructions.getLast(), 2));

		m.visitLabel(labelCaseRS);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 4);
		m.visitLdcInsn("R(String)");
		final Label labelAfter = new Label();
		m.visitJumpInsn(Opcodes.GOTO, labelAfter);

		m.visitLabel(labelCaseRI);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		m.visitVarInsn(Opcodes.ASTORE, 5);
		m.visitLdcInsn("R(Integer)");
		m.visitJumpInsn(Opcodes.GOTO, labelAfter);

		m.visitLabel(labelCaseRR);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitTypeInsn(Opcodes.CHECKCAST, "R");
		m.visitVarInsn(Opcodes.ASTORE, 6);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitLabel(labelComponent2Start);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "R", "component",
				"()Ljava/lang/Object;", false);
		m.visitLabel(labelComponent2End);
		m.visitVarInsn(Opcodes.ASTORE, 11);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 12);
		m.visitVarInsn(Opcodes.ALOAD, 11);
		m.visitVarInsn(Opcodes.ILOAD, 12);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("Ljava/lang/String;"),
						Type.getType("Ljava/lang/Integer;") });
		final Label labelInnerRestart = new Label();
		final Label labelCaseRRS = new Label();
		final Label labelCaseRRI = new Label();
		m.visitTableSwitchInsn(-1, 1, labelInnerRestart,
				new Label[] { labelInnerRestart, labelCaseRRS, labelCaseRRI });
		final Range innerSwitch2 = new Range(m.instructions.getLast(),
				m.instructions.getLast());
		replacements.add(new Replacement(2, m.instructions.getLast(), 1));
		replacements.add(new Replacement(3, m.instructions.getLast(), 2));

		m.visitLabel(labelCaseRRS);
		m.visitVarInsn(Opcodes.ALOAD, 11);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 7);
		m.visitLdcInsn("R(R(String))");
		m.visitJumpInsn(Opcodes.GOTO, labelAfter);

		m.visitLabel(labelCaseRRI);
		m.visitVarInsn(Opcodes.ALOAD, 11);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		m.visitVarInsn(Opcodes.ASTORE, 8);
		m.visitLdcInsn("R(R(Integer))");
		m.visitJumpInsn(Opcodes.GOTO, labelAfter);

		m.visitLabel(labelInnerRestart);
		final Range innerRestart = new Range();
		innerRestart.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ICONST_3);
		m.visitVarInsn(Opcodes.ISTORE, 10);
		m.visitJumpInsn(Opcodes.GOTO, labelInner);
		innerRestart.toInclusive = m.instructions.getLast();

		m.visitLabel(labelOuterRestart);
		final Range outerRestart = new Range();
		outerRestart.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, labelOuter);
		outerRestart.toInclusive = m.instructions.getLast();

		m.visitLabel(labelCaseS);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 9);
		m.visitLdcInsn("String");
		m.visitJumpInsn(Opcodes.GOTO, labelAfter);

		m.visitLabel(labelCaseI);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		m.visitVarInsn(Opcodes.ASTORE, 10);
		m.visitLdcInsn("Integer");
		m.visitJumpInsn(Opcodes.GOTO, labelAfter);

		m.visitLabel(labelCaseDefault);
		m.visitLdcInsn("default");
		m.visitJumpInsn(Opcodes.GOTO, labelAfter);

		m.visitLabel(labelAfter);
		m.visitInsn(Opcodes.ARETURN);

		m.visitLabel(labelHandler);
		final Range handler = new Range();
		handler.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/MatchException");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"toString", "()Ljava/lang/String;", false);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
				"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		handler.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);
		assertReplacedBranches(m, outerSwitch, replacements);
		assertIgnored(m, handler, handler, innerSwitch2, innerRestart,
				innerSwitch1, outerRestart);
	}

}
