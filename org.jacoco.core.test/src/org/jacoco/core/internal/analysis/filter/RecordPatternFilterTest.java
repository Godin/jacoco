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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link RecordPatternFilter}.
 */
public class RecordPatternFilterTest extends FilterTestBase {

	private final IFilter filter = new RecordPatternFilter();

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
	 * record Container(Object component) {}
	 *
	 * void example(Object o) {
	 *   switch (o) {
	 *     case Container(String c) -> ...
	 *     case Container(Integer c) -> ...
	 *     case Container(Container(String c)) -> ...
	 *     case Container(Container(Integer c)) -> ...
	 *     case String s -> ...
	 *     default -> ...
	 *   }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_nested() {
		MethodNode m = new MethodNode();
		m.visitCode();
		Label label0 = new Label();
		Label label1 = new Label();
		Label label2 = new Label();
		m.visitTryCatchBlock(label0, label1, label2, "java/lang/Throwable");
		Label label3 = new Label();
		Label label4 = new Label();
		m.visitTryCatchBlock(label3, label4, label2, "java/lang/Throwable");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects",
				"requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;",
				false);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		Label label5 = new Label();
		m.visitLabel(label5);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType(
						"Lorg/jacoco/core/test/validation/java21/targets/RecordPatternsTarget$Container;"),
						Type.getType("Ljava/lang/String;") });
		Label label6 = new Label();
		Label label7 = new Label();
		Label label8 = new Label();
		// TODO remap
		m.visitLookupSwitchInsn(label8, new int[] { 0, 1 },
				new Label[] { label6, label7 });
		m.visitLabel(label6);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST,
				"org/jacoco/core/test/validation/java21/targets/RecordPatternsTarget$Container");
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitLabel(label0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"org/jacoco/core/test/validation/java21/targets/RecordPatternsTarget$Container",
				"component", "()Ljava/lang/Object;", false);
		m.visitLabel(label1);
		m.visitVarInsn(Opcodes.ASTORE, 9);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 10);
		Label label9 = new Label();
		m.visitLabel(label9);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitVarInsn(Opcodes.ILOAD, 10);
		m.visitInvokeDynamicInsn("typeSwitch", "(Ljava/lang/Object;I)I",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/SwitchBootstraps", "typeSwitch",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType("Ljava/lang/String;"),
						Type.getType("Ljava/lang/Integer;"), Type.getType(
								"Lorg/jacoco/core/test/validation/java21/targets/RecordPatternsTarget$Container;") });
		Label label10 = new Label();
		Label label11 = new Label();
		Label label12 = new Label();
		Label label13 = new Label();
		// TODO remap
		m.visitTableSwitchInsn(-1, 2, label10,
				new Label[] { label10, label11, label12, label13 });
		final Range nestedTypeSwitch1 = new Range(m.instructions.getLast(),
				m.instructions.getLast());
		m.visitLabel(label11);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 4);
		m.visitLdcInsn("Container(String)");
		Label label14 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label14);
		m.visitLabel(label12);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		m.visitVarInsn(Opcodes.ASTORE, 5);
		m.visitLdcInsn("Container(Integer)");
		m.visitJumpInsn(Opcodes.GOTO, label14);
		m.visitLabel(label13);
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitTypeInsn(Opcodes.CHECKCAST,
				"org/jacoco/core/test/validation/java21/targets/RecordPatternsTarget$Container");
		m.visitVarInsn(Opcodes.ASTORE, 6);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitLabel(label3);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"org/jacoco/core/test/validation/java21/targets/RecordPatternsTarget$Container",
				"component", "()Ljava/lang/Object;", false);
		m.visitLabel(label4);
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
		Label label15 = new Label();
		Label label16 = new Label();
		Label label17 = new Label();
		// TODO remap
		m.visitTableSwitchInsn(-1, 1, label15,
				new Label[] { label15, label16, label17 });
		final Range nestedTypeSwitch2 = new Range(m.instructions.getLast(),
				m.instructions.getLast());
		m.visitLabel(label16);
		m.visitVarInsn(Opcodes.ALOAD, 11);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 7);
		m.visitLdcInsn("Container(Container(String))");
		m.visitJumpInsn(Opcodes.GOTO, label14);
		m.visitLabel(label17);
		m.visitVarInsn(Opcodes.ALOAD, 11);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		m.visitVarInsn(Opcodes.ASTORE, 8);
		m.visitLdcInsn("Container(Container(Integer))");
		m.visitJumpInsn(Opcodes.GOTO, label14);
		m.visitLabel(label15);
		final Range range1 = new Range();
		range1.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ICONST_3);
		m.visitVarInsn(Opcodes.ISTORE, 10);
		m.visitJumpInsn(Opcodes.GOTO, label9);
		range1.toInclusive = m.instructions.getLast();
		m.visitLabel(label10);
		final Range range2 = new Range();
		range2.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, label5);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(label7);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitVarInsn(Opcodes.ASTORE, 9);
		m.visitLdcInsn("String");
		m.visitJumpInsn(Opcodes.GOTO, label14);
		m.visitLabel(label8);
		m.visitLdcInsn("default");
		m.visitJumpInsn(Opcodes.GOTO, label14);
		m.visitLabel(label14);
		m.visitInsn(Opcodes.ARETURN);
		m.visitLabel(label2);
		final Range range3 = new Range();
		range3.fromInclusive = m.instructions.getLast();
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
		range3.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(m, //
				range3, //
				range3, //
				nestedTypeSwitch2, //
				range1, //
				nestedTypeSwitch1, //
				range2);
	}

}
