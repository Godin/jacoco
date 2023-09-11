package org.jacoco.core.test.validation.kotlin;

import static org.objectweb.asm.Opcodes.*;

import org.jacoco.core.test.Jit;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.kotlin.targets.KotlinSynchronizedTarget;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

	@Test
	public void example() throws Exception {
		final byte[] classBytes = exampleClass();
		Assert.assertEquals(run(classBytes), "");
		// TODO visitJumpInsnWithProbe ?
		Assert.assertTrue(run(Jit.instrument(classBytes))
				.contains("Monitor mismatch in method  Main::main:"
						+ " non-empty monitor stack at exceptional exit"));
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
	 * TODO check if possible to create Java or Kotlin code that compiles into
	 * the similar bytecode - see {@link #xxx(Object)}
	 */
	private static byte[] exampleClass() {
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
		return classWriter.toByteArray();
	}

	/**
	 * Mimics
	 * <a href="https://github.com/jacoco/jacoco/issues/1381">reproducer</a>
	 * Incorrectly inserted probes in this case can be safely omitted without
	 * impact on coverage result, but not in {@link #exampleClass()}
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
		Assert.assertTrue(Jit.run(classWriter.toByteArray(), true, "Main")
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
		return Jit.run(classBytes, false, "Main");
	}

	@Test
	public void testKotlin() throws Exception {
		byte[] classBytes = TargetLoader
				.getClassDataAsBytes(KotlinSynchronizedTarget.class);
		classBytes = Jit.instrument(classBytes);
		String output = Jit.run(classBytes, false,
				KotlinSynchronizedTarget.class.getName());
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
