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
package org.jacoco.core.internal.instr;

import static org.objectweb.asm.Opcodes.*;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @deprecated
 */
@Deprecated
public class StructuredLockingTest {

	@org.junit.Ignore
	@Test
	public void wip() throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(
				"/Users/evgeny.mandrikov/projects/jacoco/jacoco-issue-1381/original/Example.class");
		fileOutputStream.write(createClass());
		fileOutputStream.close();
	}

	public byte[] createClass() {
		ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(V1_7, ACC_PUBLIC, "Example", null, "java/lang/Object",
				new String[0]);

		classWriter.visitField(ACC_STATIC, "lock", "Ljava/lang/Object;", null,
				null);

		MethodVisitor methodVisitor;
		{
			methodVisitor = classWriter.visitMethod(ACC_STATIC, "nop", "()V",
					null, null);
			methodVisitor.visitCode();
			methodVisitor.visitInsn(RETURN);
			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>",
					"()V", null, null);
			methodVisitor.visitCode();
			methodVisitor.visitTypeInsn(NEW, "java/lang/Object");
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
					"<init>", "()V", false);
			methodVisitor.visitFieldInsn(PUTSTATIC, "Example", "lock",
					"Ljava/lang/Object;");
			methodVisitor.visitInsn(RETURN);
			methodVisitor.visitMaxs(2, 0);
			methodVisitor.visitEnd();
		}
		{ // https://github.com/jacoco/jacoco/issues/626#issuecomment-349466549
			methodVisitor = classWriter.visitMethod(ACC_STATIC, "oldTarget",
					"()V", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			Label label1 = new Label();
			Label label2 = new Label();
			methodVisitor.visitTryCatchBlock(label0, label1, label2, null);
			Label label3 = new Label();
			methodVisitor.visitTryCatchBlock(label2, label3, label2, null);
			Label label4 = new Label();
			Label label5 = new Label();
			methodVisitor.visitTryCatchBlock(label4, label1, label5, null);
			Label label6 = new Label();
			methodVisitor.visitTryCatchBlock(label2, label6, label5, null);
			methodVisitor.visitLabel(label4);
			methodVisitor.visitLineNumber(6, label4);
			methodVisitor.visitFieldInsn(GETSTATIC, "Example", "lock",
					"Ljava/lang/Object;");
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitVarInsn(ASTORE, 0);
			methodVisitor.visitInsn(MONITORENTER);
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(7, label0);
			methodVisitor.visitInsn(ICONST_0);
			methodVisitor.visitVarInsn(ISTORE, 1);
			Label label7 = new Label();
			methodVisitor.visitLabel(label7);
			methodVisitor.visitVarInsn(ILOAD, 1);
			methodVisitor.visitLdcInsn(new Integer(10000000));
			Label label8 = new Label();
			methodVisitor.visitJumpInsn(IF_ICMPGE, label8);
			Label label9 = new Label();
			methodVisitor.visitLabel(label9);
			methodVisitor.visitLineNumber(8, label9);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "Example", "nop", "()V",
					false);
			Label label10 = new Label();
			methodVisitor.visitLabel(label10);
			methodVisitor.visitLineNumber(7, label10);
			methodVisitor.visitIincInsn(1, 1);
			methodVisitor.visitJumpInsn(GOTO, label7);
			methodVisitor.visitLabel(label8);
			methodVisitor.visitLineNumber(10, label8);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitInsn(MONITOREXIT);
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(13, label1);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "Example", "nop", "()V",
					false);
			Label label11 = new Label();
			methodVisitor.visitLabel(label11);
			methodVisitor.visitLineNumber(10, label11);
			methodVisitor.visitInsn(RETURN);
			methodVisitor.visitLabel(label2);
			methodVisitor.visitLineNumber(11, label2);
			methodVisitor.visitVarInsn(ASTORE, 2);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitInsn(MONITOREXIT);
			methodVisitor.visitLabel(label3);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitInsn(ATHROW);
			methodVisitor.visitLabel(label5);
			methodVisitor.visitLineNumber(13, label5);
			methodVisitor.visitVarInsn(ASTORE, 3);
			methodVisitor.visitLabel(label6);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "Example", "nop", "()V",
					false);
			Label label12 = new Label();
			methodVisitor.visitLabel(label12);
			methodVisitor.visitLineNumber(14, label12);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitInsn(ATHROW);
			methodVisitor.visitMaxs(2, 4);
			methodVisitor.visitEnd();
		}
		{ // mimic https://github.com/jacoco/jacoco/issues/1381
			methodVisitor = classWriter.visitMethod(ACC_STATIC, "target", "()V",
					null, null);
			methodVisitor.visitCode();

			Label label0 = new Label();
			Label label1 = new Label();
			final Label handler = new Label();
			methodVisitor.visitTryCatchBlock(label0, label1, handler, null);

			final Label weirdCatchStart = new Label();
			final Label weirdCatchEnd = new Label();
			final Label weirdCatchHandler = new Label();
			methodVisitor.visitTryCatchBlock(weirdCatchStart, weirdCatchEnd,
					weirdCatchHandler, "java/lang/Exception");

			final Label weirdCatch2Start = new Label();
			final Label weirdCatch2End = new Label();
			methodVisitor.visitTryCatchBlock(weirdCatch2Start, weirdCatch2End,
					weirdCatchHandler, "java/lang/Exception");

			Label label4 = new Label();
			methodVisitor.visitLabel(label4);
			methodVisitor.visitFieldInsn(GETSTATIC, "Example", "lock",
					"Ljava/lang/Object;");
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitVarInsn(ASTORE, 0);
			methodVisitor.visitInsn(MONITORENTER);
			methodVisitor.visitLabel(label0);
			methodVisitor.visitInsn(ICONST_0);
			methodVisitor.visitVarInsn(ISTORE, 1);
			Label label5 = new Label();
			methodVisitor.visitLabel(label5);
			methodVisitor.visitVarInsn(ILOAD, 1);
			methodVisitor.visitLdcInsn(new Integer(10000000));
			Label label6 = new Label();
			methodVisitor.visitJumpInsn(IF_ICMPGE, label6);
			Label label7 = new Label();
			methodVisitor.visitLabel(label7);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "Example", "nop", "()V",
					false);
			Label label8 = new Label();
			methodVisitor.visitLabel(label8);
			methodVisitor.visitIincInsn(1, 1);
			methodVisitor.visitJumpInsn(GOTO, label5);
			methodVisitor.visitLabel(label6);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitLabel(label1);

			// probe is inserted here
			methodVisitor.visitLabel(weirdCatchStart);
			if (false) {
				simulateProbe(methodVisitor);
			}
			methodVisitor.visitInsn(MONITOREXIT);
			// usually monitorexit is covered by catch all
			// methodVisitor.visitLabel(label1);
			Label label9 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label9);
			methodVisitor.visitLabel(weirdCatchEnd);

			methodVisitor.visitLabel(handler);
			methodVisitor.visitVarInsn(ASTORE, 2);
			// probe is inserted here
			methodVisitor.visitLabel(weirdCatch2Start);
			if (true) {
				final Label probeStart = new Label();
				final Label probeCatch = new Label();
				final Label probeEnd = new Label();

				// even with exception handler JIT reports
				// "monitor stack height merge conflict"
				methodVisitor.visitTryCatchBlock(probeStart, probeCatch,
						probeCatch, null);
				methodVisitor.visitLabel(probeStart);
				simulateProbe(methodVisitor); // 4 - 7 bytes
				// next GOTO can't be replaced by ACONST_NULL
				// "Exception handler can be reached by both normal and
				// exceptional control flow"
				// https://issuetracker.google.com/issues/296916426
				// methodVisitor.visitInsn(Opcodes.ACONST_NULL);
				methodVisitor.visitJumpInsn(Opcodes.GOTO, probeEnd); // 3 bytes
				methodVisitor.visitLabel(probeCatch);
				methodVisitor.visitInsn(Opcodes.ATHROW); // 1 byte
				methodVisitor.visitLabel(probeEnd);
			}
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitInsn(MONITOREXIT);
			methodVisitor.visitInsn(ATHROW);
			methodVisitor.visitLabel(weirdCatch2End);

			methodVisitor.visitLabel(label9);
			methodVisitor.visitInsn(RETURN);

			methodVisitor.visitLabel(weirdCatchHandler);
			methodVisitor.visitInsn(ATHROW);

			methodVisitor.visitMaxs(3, 3);
			methodVisitor.visitEnd();
		}
		classWriter.visitEnd();

		return classWriter.toByteArray();
	}

	private static void simulateProbe(MethodVisitor mv) {
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.BASTORE);
	}

}
