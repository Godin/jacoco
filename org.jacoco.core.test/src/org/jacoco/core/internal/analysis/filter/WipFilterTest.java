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

import static org.objectweb.asm.Opcodes.*;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class WipFilterTest extends FilterTestBase {

	private final IFilter filter = new TryWithResourcesJavac11Filter();

	/**
	 * ASMified WipTarget
	 */
	@Test
	public void test() {
		Range range1 = new Range();
		Range range2 = new Range();
		Range range3 = new Range();
		MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0, "name",
				"()V", null, null);
		m.visitCode();
		Label label0 = new Label();
		Label label1 = new Label();
		Label label2 = new Label();
		m.visitTryCatchBlock(label0, label1, label2, "java/lang/Throwable");
		Label label3 = new Label();
		Label label4 = new Label();
		Label label5 = new Label();
		m.visitTryCatchBlock(label3, label4, label5, "java/lang/Throwable");
		Label label6 = new Label();
		Label label7 = new Label();
		Label label8 = new Label();
		m.visitTryCatchBlock(label6, label7, label8, "java/lang/Exception");
		Label label9 = new Label();
		Label label10 = new Label();
		m.visitTryCatchBlock(label9, label10, label8, "java/lang/Exception");
		m.visitLabel(label6);
		m.visitLineNumber(24, label6);
		m.visitVarInsn(ILOAD, 0);
		m.visitMethodInsn(INVOKESTATIC,
				"org/jacoco/core/test/validation/java7/targets/WipTarget",
				"factory", "(I)Ljava/io/Closeable;", false);
		m.visitVarInsn(ASTORE, 1);
		m.visitLabel(label0);
		m.visitLineNumber(25, label0);
		m.visitVarInsn(ALOAD, 1);
		m.visitJumpInsn(IFNONNULL, label9);
		Label label11 = new Label();
		m.visitLabel(label11);
		m.visitLineNumber(26, label11);
		m.visitLdcInsn("");
		m.visitVarInsn(ASTORE, 2);
		m.visitLabel(label1);
		range3.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(28, label1); // FIXME
		m.visitVarInsn(ALOAD, 1);
		m.visitJumpInsn(IFNULL, label7);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "java/io/Closeable", "close", "()V",
				true);
		range3.toInclusive = m.instructions.getLast();
		m.visitLabel(label7);
		m.visitLineNumber(26, label7);
		m.visitFrame(Opcodes.F_APPEND, 2,
				new Object[] { "java/io/Closeable", "java/lang/String" }, 0,
				null);
		m.visitVarInsn(ALOAD, 2);
		m.visitInsn(ARETURN);
		m.visitLabel(label9);
		m.visitLineNumber(28, label9); // FIXME
		m.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
		m.visitVarInsn(ALOAD, 1);
		range1.fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(IFNULL, label10);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "java/io/Closeable", "close", "()V",
				true);
		m.visitJumpInsn(GOTO, label10);
		range1.toInclusive = m.instructions.getLast();
		m.visitLabel(label2);
		range2.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(24, label2);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "java/lang/Throwable" });
		m.visitVarInsn(ASTORE, 2);
		m.visitVarInsn(ALOAD, 1);
		Label label12 = new Label();
		m.visitJumpInsn(IFNULL, label12);
		m.visitLabel(label3);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "java/io/Closeable", "close", "()V",
				true);
		m.visitLabel(label4);
		m.visitJumpInsn(GOTO, label12);
		m.visitLabel(label5);
		m.visitFrame(Opcodes.F_FULL, 3,
				new Object[] { Opcodes.INTEGER, "java/io/Closeable",
						"java/lang/Throwable" },
				1, new Object[] { "java/lang/Throwable" });
		m.visitVarInsn(ASTORE, 3);
		m.visitVarInsn(ALOAD, 2);
		m.visitVarInsn(ALOAD, 3);
		m.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "addSuppressed",
				"(Ljava/lang/Throwable;)V", false);
		m.visitLabel(label12);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ALOAD, 2);
		m.visitInsn(ATHROW);
		range2.toInclusive = m.instructions.getLast();

		m.visitLabel(label10);
		m.visitLineNumber(31, label10);
		m.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
		Label label13 = new Label();
		m.visitJumpInsn(GOTO, label13);
		m.visitLabel(label8);
		m.visitLineNumber(29, label8);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "java/lang/Exception" });
		m.visitVarInsn(ASTORE, 1);
		Label label14 = new Label();
		m.visitLabel(label14);
		m.visitLineNumber(30, label14);
		m.visitMethodInsn(INVOKESTATIC,
				"org/jacoco/core/test/validation/targets/Stubs", "nop", "()V",
				false);
		m.visitLabel(label13);
		m.visitLineNumber(32, label13);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitLdcInsn("");
		m.visitInsn(ARETURN);
		Label label15 = new Label();
		m.visitLabel(label15);
		m.visitLocalVariable("r", "Ljava/io/Closeable;", null, label0, label10,
				1);
		m.visitLocalVariable("e", "Ljava/lang/Exception;", null, label14,
				label13, 1);
		m.visitLocalVariable("i", "I", null, label6, label15, 0);
		m.visitMaxs(2, 4);
		m.visitEnd();

		filter.filter(m, context, output);

		assertIgnored(range1, range2, range3);
	}

}
