package org.jacoco.core.test.validation.java7;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
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
		Assert.assertTrue(run(instrument(classBytes))
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
		Assert.assertTrue(run(instrument(classBytes))
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
		System.out.println(run(classBytes));
		Assert.assertTrue(run(classBytes)
				.contains("Monitor mismatch in method  Main::main:"
						// There are multiple reasons
						// monitor stack underflow
						// improper monitor pair
						+ " "));
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
		Assert.assertTrue(run(classWriter.toByteArray(), true)
				.contains("compilation bailout:"
						+ " Exception handler can be reached by both normal and exceptional control flow"));
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
		return run(classBytes, false);
	}

	private static String run(final byte[] classBytes,
			final boolean printCompilation) throws Exception {
		final File workDir = new File("target/generated-tests");
		if (!workDir.isDirectory()) {
			Assert.assertTrue(workDir.mkdir());
		}
		try (FileOutputStream fos = new FileOutputStream(
				new File(workDir, "/Main.class"))) {
			fos.write(classBytes);
		}
		final File logFile = new File(workDir, "/log.txt");
		final List<String> command = new ArrayList<>();
		command.add(System.getProperty("java.home") + File.separator + "bin"
				+ File.separator + "java");

		if (printCompilation) {
			command.add("-XX:+PrintCompilation");
		}

		// TODO for compilation of method it should be invoked
		command.add("-Xcomp");

		// TODO requires Java 11 ?
		command.add("-Xlog:monitormismatch=info");

		command.add("-cp");
		command.add(workDir.getAbsolutePath());
		// "-cp", System.getProperty("java.class.path") + ":/tmp", //

		command.add("Main");
		new ProcessBuilder().command(command).inheritIO() //
				.redirectOutput(logFile) //
				.start() //
				.waitFor();
		return filesReadString(logFile);
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

	private static String filesReadString(File file) throws Exception {
		try (FileInputStream fis = new FileInputStream(file)) {
			return new String(InputStreams.readFully(fis),
					StandardCharsets.UTF_8);
		}
	}

	/**
	 * @return instrumented classBytes
	 */
	private static byte[] instrument(final byte[] classBytes)
			throws IOException {
		return new Instrumenter(new IExecutionDataAccessorGenerator() {
			@Override
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
