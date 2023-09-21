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

import org.jacoco.core.internal.flow.AsmUtils;
import org.jacoco.core.internal.instr.Monitors;
import org.jacoco.core.test.Jit;
import org.jacoco.core.test.JvmProcess;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.kotlin.targets.KotlinSynchronizedTarget;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * @deprecated replace by {@link JvmStructuredLockingTest}
 */
@org.junit.Ignore
@Deprecated
public class JitTest {

	/**
	 * Tests the testing infrastructure itself - other tests can not be trusted
	 * if this one fails.
	 */
	@Test
	public void sanity() throws Exception {
		Assert.assertEquals(run(noMonitorMismatchClass()), "");
		// TODO unclear AssertionError message
		Assert.assertTrue(run(monitorMismatchClass())
				.contains("Monitor mismatch in method  Main::main:"
						+ " non-empty monitor stack at exceptional exit"));
	}

	@Test
	public void reproducer() throws Exception {
		final byte[] classBytes = reproducerClass();
		Assert.assertEquals(run(classBytes), "");
		Assert.assertTrue(run(Jit.instrument(classBytes))
				.contains("Monitor mismatch in method  Main::main:"
						// There are multiple reasons
						// non-empty monitor stack at exceptional exit
						// monitor stack height merge conflict
						+ " "));
	}

	/**
	 * This requires change in
	 * {@link org.jacoco.core.internal.flow.MethodProbesAdapter#visitLabel(Label)}
	 *
	 * TODO maybe be easier to hack
	 * {@link org.jacoco.core.internal.instr.MethodInstrumenter#accept(MethodNode, MethodVisitor)}
	 * than {@link org.jacoco.core.internal.flow.MethodProbesAdapter}
	 *
	 * MethodProbesAdapter moves some labels, so that MethodInstrumenter might
	 * insert probe before or after original label and hence can't figure out
	 * whether it is inside or outside of protected region?
	 *
	 * Probe is always inserted after the start and end labels of try-catch
	 * {@link org.jacoco.core.internal.flow.MethodProbesAdapter#visitTryCatchBlock(Label, Label, Label, String)}
	 * But not easy to override MethodInstrumenter accept method.
	 *
	 * To count instructions MethodProbesAdapter should override more methods of
	 * MethodVisitor.
	 *
	 * But AnalyzerAdapter injects additional labels.
	 *
	 * Also MethodProbesAdapter should receive instance of Monitors.
	 */
	@Test
	public void non_zero_monitors_at_unprotected() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();

		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);

		Label start = new Label();
		Label end = new Label();
		Label handler1 = new Label();
		mv.visitTryCatchBlock(start, end, handler1, null);
		mv.visitLabel(start);
		mv.visitInsn(NOP);
		mv.visitLabel(end);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);

		Label handler2 = new Label();
		mv.visitTryCatchBlock(handler1, handler2, handler2,
				"java/lang/Exception");
		mv.visitLabel(handler1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(ATHROW);

		mv.visitLabel(handler2);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		final byte[] classBytes = classWriter.toByteArray();

		Assert.assertEquals("", run(classBytes));
		// FIXME this test supposed to fail similarly to reproducer, but doesn't
		// because code of handler1 is slightly different
		Assert.assertEquals("", run(Jit.instrument(classBytes)));
	}

	/**
	 * This requires change in
	 * {@link org.jacoco.core.internal.flow.MethodProbesAdapter#visitLabel(Label)}
	 */
	@Test
	public void zero_monitors_at_protected() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);

		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();
		mv.visitTryCatchBlock(start, end, handler, null);
		mv.visitLabel(start);
		mv.visitInsn(NOP);
		mv.visitLabel(end);
		mv.visitInsn(RETURN);

		mv.visitLabel(handler);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();

		// FIXME insertion of probe in this case turns unreachable handler into
		// reachable
		// TODO should be possible to construct case when handler was reachable
		Monitors.compute(
				AsmUtils.classBytesToClassNode(classBytes).methods.get(2))
				.print();

		Assert.assertEquals("", run(classBytes));
		// TODO inability insert not detected??? because we consider handler as
		// reachable, and it leads to underflow
		Assert.assertEquals("", run(Jit.instrument(classBytes)));
	}

	/**
	 * This requires change in
	 * {@link org.jacoco.core.internal.flow.MethodProbesAdapter#visitInsn(int)}
	 */
	@Test
	public void zero_monitors_at_protected_return() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitVarInsn(ASTORE, 1);
		Label start = new Label();
		Label handler = new Label();
		mv.visitTryCatchBlock(start, handler, handler, null);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitLabel(start);
		mv.visitInsn(RETURN);
		mv.visitLabel(handler);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();

		// FIXME insertion of probe in this case turns unreachable handler into
		// reachable
		// TODO should be possible to construct case when handler was reachable
		Monitors.compute(
				AsmUtils.classBytesToClassNode(classBytes).methods.get(2))
				.print();

		Assert.assertEquals("", run(classBytes));
		Assert.assertEquals("", run(Jit.instrument(classBytes)));
	}

	/**
	 * This requires change in
	 * {@link org.jacoco.core.internal.flow.MethodProbesAdapter#visitJumpInsn(int, Label)}
	 */
	@Test
	public void zero_monitors_at_protected_jump() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 1);
		mv.visitInsn(MONITORENTER);

		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();
		mv.visitTryCatchBlock(start, end, handler, null);

		mv.visitLabel(start);
		// force insertion of a probe
		mv.visitVarInsn(ALOAD, /* argument */ 0);
		mv.visitJumpInsn(IFNONNULL, end);
		mv.visitLabel(end);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);

		mv.visitLabel(handler);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(ATHROW);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();

		Monitors.compute(
				AsmUtils.classBytesToClassNode(classBytes).methods.get(2))
				.print();

		Assert.assertEquals("", run(classBytes));
		Assert.assertEquals("", run(Jit.instrument(classBytes)));
	}

	/**
	 * TODO check if possible to create Java or Kotlin code that compiles into
	 * the similar bytecode - see {@link #xxx(Object)}
	 *
	 * This requires change in
	 * {@link org.jacoco.core.internal.flow.MethodProbesAdapter#visitJumpInsn(int, Label)}
	 *
	 * @deprecated replaced by {@link JvmStructuredLockingTest#test()}
	 */
	@Test
	public void non_zero_monitors_at_unprotected_jump() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 1);
		mv.visitInsn(MONITORENTER);

		// force insertion of a probe
		mv.visitVarInsn(ALOAD, /* argument */ 0);
		Label label = new Label();
		mv.visitJumpInsn(IFNONNULL, label);
		mv.visitLabel(label);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();

		Assert.assertEquals("", run(classBytes));
		// FIXME requires change in visitJumpInsnWithProbe
		Assert.assertEquals("", run(Jit.instrument(classBytes)));
	}

	/**
	 * This test demonstrates that HotSpot JVM requires monitorexit to pop.
	 */
	@Test
	public void pairs() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(AALOAD);
		mv.visitVarInsn(ASTORE, 1);

		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(AALOAD);
		mv.visitVarInsn(ASTORE, 2);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);
		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();
		mv.visitTryCatchBlock(start, end, handler, null);
		mv.visitLabel(start);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitInsn(MONITORENTER);
		mv.visitLabel(end);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);

		mv.visitVarInsn(ALOAD, 2);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);

		mv.visitLabel(handler);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("", run(classBytes));
	}

	@Test
	public void split() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ASTORE, 2);
		Label next = new Label();
		Label target = new Label();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitJumpInsn(IFNULL, next);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitJumpInsn(GOTO, target);
		mv.visitLabel(next);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitLabel(target);
		mv.visitInsn(MONITORENTER);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("", run(classBytes));
	}

	@Test
	public void test() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(run(classBytes)
				.contains("Monitor mismatch in method  Main::main:"
						// There are multiple reasons
						// non-empty monitor stack at exceptional exit
						// nested redundant lock -- bailout...
						+ " "));
	}

	@Test
	public void test2() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(run(classBytes)
				.contains("Monitor mismatch in method  Main::main:"
						// There are multiple reasons
						// non-empty monitor stack at exceptional exit
						// non-empty monitor stack at return
						+ " "));
	}

	@Test
	public void test3() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		final String output = run(classBytes);
		System.out.println(output);
		Assert.assertTrue(
				output.contains("Monitor mismatch in method  Main::main:"
						// There are multiple reasons
						// monitor stack underflow
						// improper monitor pair
						+ " "));
	}

	/**
	 * HotSpot tracks monitors in locals and on stack.
	 */
	@Test
	public void test5() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitInsn(MONITORENTER);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		final String output = run(classBytes);
		Assert.assertTrue(output.contains(
				"Monitor mismatch in method  Main::main: improper monitor pair"));
	}

	@Test
	public void test4() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITORENTER);
		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();
		Label handler2 = new Label();
		Label exit = new Label();
		// mv.visitTryCatchBlock(start, end, handler2,
		// "java/lang/ArithmeticException");
		// mv.visitTryCatchBlock(start, end, handler2, "java/lang/Throwable");
		mv.visitTryCatchBlock(start, end, handler2, null);
		// mv.visitTryCatchBlock(start, end, handler, null);
		mv.visitLabel(start);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IDIV);
		mv.visitInsn(Opcodes.POP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLabel(end);
		mv.visitLabel(handler);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(MONITOREXIT);

		mv.visitLabel(exit);
		mv.visitInsn(RETURN);
		mv.visitLabel(handler2);
		// mv.visitVarInsn(ALOAD, 1);
		// mv.visitInsn(MONITOREXIT);
		// mv.visitInsn(ATHROW);
		mv.visitJumpInsn(Opcodes.GOTO, exit);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		System.out.println(run(classBytes));
	}

	/**
	 * Java compiler creates protected region.
	 */
	public static boolean xxx(Object arg) {
		synchronized (new Object()) {
		}

		synchronized (new Object()) {
			return arg == null;
		}
	}

	/**
	 * Mimics
	 * <a href="https://github.com/jacoco/jacoco/issues/1381">reproducer</a>
	 * Incorrectly inserted probes in this case can be safely omitted without
	 * impact on coverage result, but not in
	 * {@link #non_zero_monitors_at_unprotected_jump()}
	 */
	private static byte[] reproducerClass() {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();

		Label protectedRegionStart = new Label();
		Label protectedRegionEnd = new Label();
		Label protectedRegionHandler = new Label();
		mv.visitTryCatchBlock(protectedRegionStart, protectedRegionEnd,
				protectedRegionHandler, null);

		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 0);
		mv.visitInsn(MONITORENTER);
		mv.visitLabel(protectedRegionStart);
		mv.visitMethodInsn(INVOKESTATIC, "Main", "nop", "()V", false);
		mv.visitVarInsn(ALOAD, 0);

		Label weirdRegion1Start = new Label();
		Label weirdRegion1End = new Label();
		Label handler = new Label();
		mv.visitTryCatchBlock(weirdRegion1Start, weirdRegion1End, handler,
				"java/lang/Exception");
		mv.visitLabel(weirdRegion1Start);
		// protected region ends right before monitorexit
		// weird region starts right at monitorexit
		mv.visitLabel(protectedRegionEnd);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitLabel(weirdRegion1End);

		mv.visitLabel(protectedRegionHandler);
		mv.visitVarInsn(ASTORE, 1);
		// This one is actually understandable
		// allows exception in protectedRegion to arrive into handler
		Label weirdRegion2Start = new Label();
		Label weirdRegion2End = new Label();
		mv.visitTryCatchBlock(weirdRegion2Start, weirdRegion2End, handler,
				"java/lang/Exception");
		mv.visitLabel(weirdRegion2Start);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(ATHROW);
		mv.visitLabel(weirdRegion2End);

		mv.visitLabel(handler);
		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}

	/**
	 * <a href="https://issuetracker.google.com/issues/296916426">See also</a>
	 */
	@Test
	public void exception_handler_can_be_reached_by_both_normal_and_exceptional_control_flow()
			throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label start = new Label();
		Label handler = new Label();
		mv.visitTryCatchBlock(start, handler, handler, null);
		mv.visitLabel(start);
		mv.visitInsn(ACONST_NULL);
		mv.visitLabel(handler);
		mv.visitInsn(POP);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();

		Assert.assertTrue(new JvmProcess() //
				.addOption("-XX:+PrintCompilation")
				.execute("Main", classWriter.toByteArray())
				.contains("compilation bailout:"
						+ " Exception handler can be reached by both normal and exceptional control flow"));
		// TODO note that "compilation bailout: exception handler covers itself"
		// is possible
	}

	/**
	 * @return bytes of a class that violates structured locking
	 */
	private static byte[] monitorMismatchClass() {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 0);
		mv.visitInsn(MONITORENTER);
		mv.visitMethodInsn(INVOKESTATIC, "Main", "nop", "()V", false);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}

	private static byte[] noMonitorMismatchClass() {
		final ClassWriter classWriter = createClassWriter();
		final MethodVisitor mv = classWriter.visitMethod(
				ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "Main", "lock", "Ljava/lang/Object;");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(MONITORENTER);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(MONITOREXIT);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		return classWriter.toByteArray();
	}

	/**
	 * @return JVM output
	 */
	private static String run(final byte[] classBytes) throws Exception {
		return new JvmProcess().execute("Main", classBytes);
	}

	@Test
	public void testKotlin() throws Exception {
		byte[] classBytes = TargetLoader
				.getClassDataAsBytes(KotlinSynchronizedTarget.class);
		classBytes = Jit.instrument(classBytes);
		String output = new JvmProcess()
				.execute(KotlinSynchronizedTarget.class.getName(), classBytes);
		Assert.assertEquals("", output);
	}

	/**
	 * <code><pre>
	 * public class Main {
	 *   static Object lock = new Object();
	 * 	 static void nop() {}
	 * }
	 * </pre></code>
	 */
	private static ClassWriter createClassWriter() {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(V1_7, ACC_PUBLIC, "Main", null, "java/lang/Object",
				new String[0]);
		classWriter.visitField(ACC_STATIC, "lock", "Ljava/lang/Object;", null,
				null);
		{
			final MethodVisitor mv = classWriter.visitMethod(ACC_STATIC,
					"<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/lang/Object");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitFieldInsn(PUTSTATIC, "Main", "lock", "Ljava/lang/Object;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 0);
			mv.visitEnd();
		}
		{
			final MethodVisitor mv = classWriter.visitMethod(ACC_STATIC, "nop",
					"()V", null, null);
			mv.visitCode();
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		return classWriter;
	}

}
