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
package org.jacoco.core.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.test.validation.JavaVersion;

public final class JvmProcess {

	private final ArrayList<String> parameters = new ArrayList<String>();

	public JvmProcess() {
		// TODO for compilation of method it should be invoked
		// TODO explicitly compile only Main using "-XX:CompileCommand" ?
		parameters.add("-Xcomp");
		// TODO requires Java 11 ?
		if (JavaVersion.current().isBefore("11")) {
			throw new AssertionError("Java 11 required");
		}
		parameters.add("-Xlog:monitormismatch=info");
	}

	public JvmProcess addOption(final String option) {
		parameters.add(option);
		return this;
	}

	/**
	 * @return JVM output
	 */
	public String execute(String mainClassName, byte[] classBytes)
			throws Exception {
		final File workDir = new File("target/generated-tests");
		final File classDir = new File(workDir,
				mainClassName.replace('.', '/'));
		classDir.mkdirs();
		final FileOutputStream fos = new FileOutputStream(new File(workDir,
				"/" + mainClassName.replace('.', '/') + ".class"));
		fos.write(classBytes);
		fos.close();

		final ArrayList<String> command = new ArrayList<String>();
		command.add(System.getProperty("java.home") + File.separator + "bin"
				+ File.separator + "java");

		command.addAll(parameters);

		command.add("-cp");
		// command.add(workDir.getAbsolutePath());
		command.add(workDir.getAbsolutePath() + ":"
				+ System.getProperty("java.class.path"));

		command.add(mainClassName);

		ProcessBuilder processBuilder = new ProcessBuilder().command(command);
		ProcessBuilder.class.getMethod("inheritIO").invoke(processBuilder);
		final File logFile = new File(workDir, "/stdout.txt");
		ProcessBuilder.class.getMethod("redirectOutput", File.class)
				.invoke(processBuilder, logFile);
		processBuilder //
				.start() //
				.waitFor();
		return filesReadString(logFile);
	}

	private static String filesReadString(File file) throws Exception {
		final FileInputStream fis = new FileInputStream(file);
		final String result = String.class
				.getConstructor(byte[].class, Charset.class).newInstance(
						InputStreams.readFully(fis), Charset.forName("UTF-8"));
		fis.close();
		return result;
	}

}
