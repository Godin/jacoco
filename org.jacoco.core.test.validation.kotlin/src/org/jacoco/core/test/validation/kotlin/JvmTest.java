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

import org.jacoco.core.test.JvmProcess;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * TODO note that this test should be in the module for which JDK 11 can be
 * selected, i.e. that is not compiled into Java 5 bytecode
 */
public class JvmTest {

	/**
	 * This test demonstrates that HotSpot JVM requires the same monitorenter
	 * instructions on all paths to monitorexit.
	 */
	@Test
	public void test() throws Exception {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", null);
		final MethodVisitor mv = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		mv.visitCode();

		Label target = new Label();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitJumpInsn(Opcodes.IFNULL, target);
		mv.visitInsn(Opcodes.MONITORENTER);
		Label after = new Label();
		mv.visitJumpInsn(Opcodes.GOTO, after);
		mv.visitLabel(target);
		mv.visitInsn(Opcodes.MONITORENTER);
		mv.visitLabel(after);

		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.MONITOREXIT);
		mv.visitInsn(Opcodes.RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		String output = new JvmProcess() //
				.addOption("-Xcomp") //
				.addOption("-XX:CompileCommand=compileonly Main::*") //
				.execute("Main", classBytes);
		System.out.println(output);
		Assert.assertTrue(output.contains(
				"Monitor mismatch in method  Main::main: improper monitor pair"));
	}

	/**
	 * TODO
	 *
	 * This test demonstrates that HotSpot JVM verifies monitors only in
	 * compiled methods.
	 *
	 * To be compiled method should be invoked?
	 *
	 * TODO such test requires to remove "-Xcomp" form {@link JvmProcess} ?
	 */
	@org.junit.Ignore
	@Test
	public void test2() throws Exception {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", null);

		MethodVisitor mv = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		mv = classWriter.visitMethod(0, "target", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.MONITORENTER);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("", new JvmProcess().execute("Main", classBytes));

		Assert.assertEquals("", new JvmProcess() //
				.addOption("-XX:+PrintCompilation")
				.addOption("-XX:CompileCommand=compileonly Main::target")
				.execute("Main", classBytes));
	}

}
