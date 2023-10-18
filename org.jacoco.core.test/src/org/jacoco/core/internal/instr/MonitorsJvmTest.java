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

import org.jacoco.core.test.JvmProcess;
import org.jacoco.core.test.validation.JavaVersion;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * TODO note that this test should be in the module for which JDK 11 can be
 * selected, i.e. that is not compiled into Java 5 bytecode
 *
 * https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-2.html#jvms-2.11.10
 */
public class MonitorsJvmTest {

	@BeforeClass
	public static void beforeAll() {
		// FIXME also HotSpot-only ? but zoo
		// https://javaalmanac.io/jdk/download/
		// https://sdkman.io/jdks
		// https://rafael.codes/openjdk/
		if (JavaVersion.current().isBefore("11")) {
			throw new AssumptionViolatedException(
					"this test requires at least Java 11");
		}
	}

	/**
	 * This test demonstrates that HotSpot JVM requires the same monitorenter
	 * instructions on all paths to monitorexit instruction.
	 */
	@Test
	public void test0() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		final Label target = new Label();
		final Label after = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitJumpInsn(Opcodes.IFNULL, target);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(target);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(after);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(execute(classBytes).contains(
				"Monitor mismatch in method  Main::main: improper monitor pair"));
	}

	/**
	 * TODO better description FIXME see {@link #test1_2()}
	 *
	 * See also
	 * https://mail.openjdk.org/pipermail/hotspot-dev/2022-October/064988.html
	 * https://github.com/openjdk/jdk/pull/9680#issuecomment-1220235459
	 * https://github.com/openjdk/jdk/pull/15925
	 *
	 * This test demonstrates that HotSpot JVM ...
	 */
	@Test
	public void test1() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				"target", "(Ljava/lang/Object;Ljava/lang/Object;)V", null,
				null);
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLabel(start);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(end);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);
		m.accept(classWriter);

		m = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Main", "target",
				"(Ljava/lang/Object;Ljava/lang/Object;)V", false);
		m.visitInsn(Opcodes.RETURN);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(execute(classBytes).contains(
				"Monitor mismatch in method  Main::target: improper monitor pair"));
	}

	/**
	 * FIXME seems that this test demonstrates that handlers are missing in
	 * {@link #test1()}
	 */
	@Test
	public void test1_2() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				"target", "(Ljava/lang/Object;Ljava/lang/Object;)V", null,
				null);
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLabel(start);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitLabel(end);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);
		m.accept(classWriter);

		m = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Main", "target",
				"(Ljava/lang/Object;Ljava/lang/Object;)V", false);
		m.visitInsn(Opcodes.RETURN);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("", execute(classBytes));
	}

	/**
	 * This test demonstrates that HotSpot JVM does not verify exception
	 * handlers whose range covers only non-throwing instructions.
	 * 
	 * @see #test_ldc()
	 * @see #test_instanceof()
	 */
	@Test
	public void test2() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(start);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLabel(end);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitInsn(Opcodes.ATHROW);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("", execute(classBytes));
	}

	@Test
	public void test_ldc() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(start);
		m.visitLdcInsn("");
		m.visitLabel(end);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitInsn(Opcodes.ATHROW);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(execute(classBytes).contains(
				"Monitor mismatch in method  Main::main: non-empty monitor stack at exceptional exit"));
	}

	@Test
	public void test_instanceof() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLabel(start);
		m.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Object");
		m.visitLabel(end);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitInsn(Opcodes.ATHROW);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(execute(classBytes).contains(
				"Monitor mismatch in method  Main::main: non-empty monitor stack at exceptional exit"));
	}

	@Test
	public void test_idea_infinite_loop() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		final Label start = new Label();
		final Label end = new Label();
		final Label end2 = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitTryCatchBlock(handler, end2, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(start);
		// m.visitInsn(Opcodes.ACONST_NULL);
		// m.visitInsn(Opcodes.ATHROW);
		m.visitLdcInsn("");
		m.visitLabel(end);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ASTORE, 0);
		m.visitTypeInsn(Opcodes.NEW, "java/lang/Throwable");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Throwable",
				"<init>", "(Ljava/lang/Throwable;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(end2);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("", execute(classBytes));
	}

	/**
	 * This test demonstrates that HotSpot JVM requires catch-all handler.
	 *
	 * @see MethodInstrumenterMonitorsTest#test2()
	 */
	@Test
	public void test3() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, end, handler, "java/lang/Throwable");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLabel(start);
		m.visitInsn(Opcodes.ARRAYLENGTH);
		m.visitLabel(end);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(execute(classBytes).contains(
				"Monitor mismatch in method  Main::main: non-empty monitor stack at exceptional exit"));
	}

	/**
	 * TODO better description
	 *
	 * This test demonstrates that stacks should be the same in all handlers,
	 * not only in catch-any.
	 *
	 * @see MethodInstrumenterMonitorsTest#test1()
	 */
	@Test
	public void test4() throws Exception {
		final ClassWriter classWriter = createClassWriter();
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		final Label start = new Label();
		final Label end = new Label();
		final Label handler1 = new Label();
		final Label handler2 = new Label();
		// TODO use ClassCastException to show that type doesn't matter,
		// type duplicates too
		m.visitTryCatchBlock(start, end, handler1, "java/lang/Throwable");
		m.visitTryCatchBlock(start, end, handler2, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitLabel(start);
		m.visitInsn(Opcodes.ARRAYLENGTH);
		m.visitLabel(end);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler1);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(handler2);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);
		m.accept(classWriter);

		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertTrue(execute(classBytes).contains(
				"Monitor mismatch in method  Main::main: non-empty monitor stack at exceptional exit"));
	}

	private static String execute(final byte[] classBytes) throws Exception {
		return new JvmProcess() //
				.addOption("-Xcomp") //
				.addOption("-XX:CompileCommand=quiet") //
				.addOption("-XX:CompileCommand=compileonly Main::*") //
				.execute("Main", classBytes);
	}

	private static ClassWriter createClassWriter() {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", null);
		return classWriter;
	}

}
