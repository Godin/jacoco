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
package org.jacoco.core.test.validation.kotlin;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.MethodInstrumenterMonitorsTest;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.test.JvmProcess;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * TODO use JVM to test that instrumentation does not break structured locking
 */
public class JvmStructuredLockingTest {

	/**
	 * TODO check if possible to create Java or Kotlin code that compiles into
	 * the similar bytecode.
	 */
	@Test
	public void test() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(MONITORENTER);

		mv.visitVarInsn(ALOAD, 0);
		final Label label = new Label();
		// can not insert probe here:
		mv.visitJumpInsn(IFNONNULL, label);
		mv.visitLabel(label);

		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();

		Assert.assertEquals("", new JvmProcess().execute("Main", classBytes));
		Assert.assertEquals("",
				new JvmProcess().execute("Main", instrument(classBytes)));
	}

	// FIXME
	@org.junit.Ignore
	@Test
	public void unreachable_handler() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		mv.visitCode();

		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();
		mv.visitTryCatchBlock(start, end, handler, null);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.MONITORENTER);
		mv.visitLabel(start);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.MONITOREXIT);
		mv.visitLabel(end);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitLabel(handler);
		mv.visitInsn(Opcodes.ATHROW);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();

		Assert.assertEquals("", new JvmProcess().execute("Main", classBytes));
		Assert.assertEquals("",
				new JvmProcess().execute("Main", instrument(classBytes)));
	}

	@Test
	public void reproducer() throws Exception {
		final byte[] classBytes = createClass();
		Assert.assertEquals("",
				new JvmProcess().execute("ExampleKt", classBytes));
		Assert.assertEquals("",
				new JvmProcess().execute("ExampleKt", instrument(classBytes)));
	}

	/**
	 * Constructs the same bytecode as if
	 *
	 * <pre>
	 * void target() {
	 * 	Object obj = new Object();
	 * 	try {
	 * 		synchronized (obj) {
	 * 			nop();
	 * 		}
	 * 	} catch (Exception e) {
	 * 	}
	 * }
	 * </pre>
	 *
	 * was compiled by Java compiler 17.0.2 and optimized by R8 4.0.63.
	 */
	private byte[] createClass() {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(V1_7, ACC_PUBLIC, "ExampleKt", null,
				"java/lang/Object", new String[0]);
		MethodVisitor mv;
		{
			mv = classWriter.visitMethod(ACC_PUBLIC | ACC_FINAL | ACC_STATIC,
					"nop", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 1);
			mv.visitEnd();
		}
		{
			mv = classWriter.visitMethod(
					ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, "main",
					"([Ljava/lang/String;)V", null, null);
			mv.visitCode();
			mv.visitMethodInsn(INVOKESTATIC, "ExampleKt", "target", "()V",
					false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 1);
			mv.visitEnd();
		}
		{
			mv = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "target",
					"()V", null, null);
			mv.visitCode();
			Label label0 = new Label();
			Label label1 = new Label();
			Label label2 = new Label();
			mv.visitTryCatchBlock(label0, label1, label2,
					"java/lang/Exception");
			Label label3 = new Label();
			mv.visitTryCatchBlock(label1, label3, label3, null);
			Label label4 = new Label();
			mv.visitTryCatchBlock(label3, label4, label3, null);
			mv.visitTryCatchBlock(label4, label2, label2,
					"java/lang/Exception");
			Label label5 = new Label();
			mv.visitLabel(label5);
			mv.visitLineNumber(4, label5);
			mv.visitTypeInsn(NEW, "java/lang/Object");
			mv.visitInsn(DUP);
			mv.visitInsn(DUP2);
			mv.visitVarInsn(ASTORE, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitLabel(label0);
			mv.visitLineNumber(6, label0);
			mv.visitInsn(MONITORENTER);
			mv.visitLabel(label1);
			mv.visitLineNumber(7, label1);
			mv.visitMethodInsn(INVOKESTATIC, "ExampleKt", "nop", "()V", false);
			Label label6 = new Label();
			mv.visitLabel(label6);
			mv.visitLineNumber(8, label6);
			mv.visitInsn(MONITOREXIT);
			Label label7 = new Label();
			mv.visitJumpInsn(GOTO, label7);
			mv.visitLabel(label3);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(MONITOREXIT);
			mv.visitLabel(label4);
			mv.visitInsn(ATHROW);
			mv.visitLabel(label2);
			mv.visitInsn(POP);
			mv.visitLabel(label7);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 2);
			mv.visitEnd();
		}
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}

	/**
	 * Constructs the same bytecode as if
	 *
	 * <pre>
	 * fun target() {
	 *   val obj = Any() // line 2
	 *   try {
	 *     synchronized(obj) { // line 4
	 *       nop() // line 5
	 *     } // line 6
	 *   } catch (e: Exception) {
	 *     nop(); // line 8
	 *   }
	 * }
	 * </pre>
	 *
	 * was compiled by Kotlin compiler 1.8.10 and optimized by R8 8.1.56
	 *
	 * TODO placement of "try" inside "synchronized" avoids the issue
	 *
	 * TODO what if we don't enforce probe at try-catch start?
	 *
	 * TODO For this case is enough to not force probe at the beginning of
	 * try-catch, but detailed analysis of original reproducer needed?
	 *
	 * TODO note that Java case seems different (at least according to my
	 * comment in jacoco-issue-1381/r8/run.sh) - see {@link #createClass()},
	 * how?
	 *
	 * @see MethodInstrumenterMonitorsTest#repro()
	 */
	@Test
	public void testRepro() throws Exception {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(V1_7, ACC_PUBLIC, "ExampleKt", null,
				"java/lang/Object", new String[0]);

		MethodVisitor method = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, "main",
				"([Ljava/lang/String;)V", null, null);
		method.visitCode();

		Label label0 = new Label();
		Label label1 = new Label();
		Label label2 = new Label();
		method.visitTryCatchBlock(label0, label1, label2,
				"java/lang/Exception");
		Label label3 = new Label();
		Label label4 = new Label();
		method.visitTryCatchBlock(label1, label3, label4, null);
		Label label5 = new Label();
		method.visitTryCatchBlock(label5, label4, label2,
				"java/lang/Exception");
		Label label6 = new Label();
		method.visitTryCatchBlock(label6, label2, label2,
				"java/lang/Exception");
		Label label7 = new Label();
		method.visitLabel(label7);
		method.visitLineNumber(2, label7);
		method.visitTypeInsn(NEW, "java/lang/Object");
		method.visitInsn(DUP);
		method.visitInsn(DUP2);
		method.visitVarInsn(ASTORE, 0);
		method.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		method.visitLabel(label0);
		// Probe[0]
		method.visitLineNumber(4, label0);
		method.visitInsn(MONITORENTER);
		method.visitLabel(label1);
		// Probe[1]
		method.visitLineNumber(5, label1); // Line 5
		method.visitMethodInsn(INVOKESTATIC, "ExampleKt", "nop", "()V", false);
		Label label8 = new Label();
		method.visitLabel(label8);
		method.visitLineNumber(6, label8); // Line 6
		method.visitFieldInsn(GETSTATIC, "kotlin/Unit", "INSTANCE",
				"Lkotlin/Unit;");
		method.visitLabel(label3);
		method.visitInsn(POP);
		method.visitLabel(label5);
		// Probe[2]
		method.visitLineNumber(4, label5);
		method.visitInsn(MONITOREXIT);
		Label label9 = new Label();
		// JumpInsnWithProbe[3]:
		method.visitJumpInsn(GOTO, label9);
		method.visitLabel(label4);
		method.visitVarInsn(ASTORE, 1);
		method.visitLabel(label6);
		// Probe[4]
		method.visitVarInsn(ALOAD, 1);
		method.visitVarInsn(ALOAD, 0);
		method.visitInsn(MONITOREXIT);
		// InsnWithProbe[5]:
		method.visitInsn(ATHROW);
		method.visitLabel(label2);
		method.visitInsn(POP);
		// Probe[6]
		Label label10 = new Label();
		method.visitLabel(label10);
		method.visitLineNumber(8, label10);
		method.visitMethodInsn(INVOKESTATIC, "ExampleKt", "nop", "()V", false);
		// Probe[7]
		method.visitLabel(label9);
		// InsnWithProbe[8]:
		method.visitInsn(RETURN);

		method.visitMaxs(0, 0);
		method.visitEnd();

		method = classWriter.visitMethod(Opcodes.ACC_STATIC, "nop", "()V", null,
				null);
		method.visitCode();
		method.visitInsn(RETURN);
		method.visitMaxs(0, 0);
		method.visitEnd();

		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();

		Assert.assertEquals("",
				new JvmProcess().execute("ExampleKt", instrument(classBytes)));
	}

	private static ClassWriter createClassWriter() {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", null);
		return classWriter;
	}

	/**
	 * @return instrumented classBytes
	 */
	private static byte[] instrument(final byte[] classBytes)
			throws IOException {
		return new Instrumenter(new IExecutionDataAccessorGenerator() {
			public int generateDataAccessor(final long classId,
					final String className, final int probeCount,
					final MethodVisitor mv) {
				InstrSupport.push(mv, probeCount);
				mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
				return 1;
			}
		}).instrument(classBytes, null);
	}

}
