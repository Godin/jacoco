package org.jacoco.core.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.test.validation.JavaVersion;
import org.junit.Assert;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class Jit {

	private Jit() {
	}

	/**
	 * @return JVM output
	 */
	public static String run(final byte[] classBytes,
			final boolean printCompilation, final String mainClassName)
			throws Exception {
		if (JavaVersion.current().isBefore("11")) {
			throw new AssertionError("Java 11 required");
		}
		final File workDir = new File("target/generated-tests");
		if (!workDir.isDirectory()) {
			Assert.assertTrue(workDir.mkdir());
		}
		final File classDir = new File(workDir,
				mainClassName.replace('.', '/'));
		classDir.mkdirs();
		FileOutputStream fos = new FileOutputStream(new File(workDir,
				"/" + mainClassName.replace('.', '/') + ".class"));
		fos.write(classBytes);
		fos.close();
		final File logFile = new File(workDir, "/log.txt");
		final ArrayList<String> command = new ArrayList<String>();
		command.add(System.getProperty("java.home") + File.separator + "bin"
				+ File.separator + "java");

		if (printCompilation) {
			command.add("-XX:+PrintCompilation");
		}

		// TODO for compilation of method it should be invoked
		command.add("-Xcomp");

		// TODO requires Java 11 ?
		command.add("-Xlog:monitormismatch=info");

		command.add("-XX:+UnlockDiagnosticVMOptions");
		command.add("-XX:+PrintInlining");

		command.add("-cp");
		// command.add(workDir.getAbsolutePath());
		command.add(workDir.getAbsolutePath() + ":"
				+ System.getProperty("java.class.path"));

		command.add(mainClassName);
		ProcessBuilder processBuilder = new ProcessBuilder().command(command);
		ProcessBuilder.class.getMethod("inheritIO").invoke(processBuilder);
		ProcessBuilder.class.getMethod("redirectOutput", File.class)
				.invoke(processBuilder, logFile);
		processBuilder //
				.start() //
				.waitFor();
		return filesReadString(logFile);
	}

	private static String filesReadString(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);
		String result = String.class.getConstructor(byte[].class, Charset.class)
				.newInstance(InputStreams.readFully(fis),
						Charset.forName("UTF-8"));
		fis.close();
		return result;
	}

	/**
	 * @return instrumented classBytes
	 */
	public static byte[] instrument(final byte[] classBytes)
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
