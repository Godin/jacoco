/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO
 * <ul>
 * <li>not suitable for testing static initializers</li>
 * <li>need to be smarter about nested classes</li>
 * </ul>
 */
public class TestSubject implements TestRule {

	private static final IAgent agent = getAgent();

	private static final Set<String> insidersClassNames = new HashSet<String>();

	private static final ByteArrayOutputStream insiders = new ByteArrayOutputStream();

	private static byte[] outsiders = new byte[0];

	private final String testSubject;

	private static IAgent getAgent() {
		try {
			final IAgent agent = RT.getAgent();
			System.out.println("Registering shutdown hook");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					shutdown();
				}
			});
			return agent;
		} catch (Exception e) {
			return null;
		}
	}

	public static TestSubject is(final Class cls) {
		return new TestSubject(cls);
	}

	private TestSubject(final Class cls) {
		this.testSubject = cls.getName().replace('.', '/');
		if (!insidersClassNames.add(testSubject)) {
			throw new UnsupportedOperationException();
		}
	}

	public Statement apply(final Statement base,
			final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before();
				try {
					base.evaluate();
				} finally {
					after();
				}
			}
		};
	}

	private void before() throws IOException {
		updateOutsiders();
	}

	private static void updateOutsiders() throws IOException {
		if (agent == null) {
			return;
		}
		final byte[] data = agent.getExecutionData(true);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final ExecutionDataFilter filter = new ExecutionDataFilter(output) {
			@Override
			boolean keep(String className) {
				return !insidersClassNames.contains(className);
			}
		};
		filter.filter(outsiders);
		filter.filter(data);
		outsiders = output.toByteArray();
	}

	private void after() throws IOException {
		if (agent == null) {
			return;
		}

		final byte[] data = agent.getExecutionData(true);
		final ExecutionDataFilter filter = new ExecutionDataFilter(insiders) {
			@Override
			boolean keep(String className) {
				if (testSubject.equals(className)) {
					System.out.println("Got data for subject");
					return true;
				}
				return false;
			}
		};
		filter.filter(data);
	}

	private static abstract class ExecutionDataFilter
			extends ExecutionDataWriter {

		ExecutionDataFilter(final OutputStream output) throws IOException {
			super(output);
		}

		abstract boolean keep(String className);

		public void visitClassExecution(final ExecutionData data) {
			if (!keep(data.getName())) {
				return;
			}
			super.visitClassExecution(data);
		}

		@Override
		public void visitSessionInfo(SessionInfo info) {
		}

		void filter(final byte[] data) throws IOException {
			final ExecutionDataReader reader = new ExecutionDataReader(
					new ByteArrayInputStream(data));
			reader.setExecutionDataVisitor(this);
			reader.setSessionInfoVisitor(this);
			reader.read();
		}
	}

	private static void shutdown() {
		if (agent == null) {
			return;
		}

		System.out.println("Executing shutdown hook");
		try {
			updateOutsiders();

			final File file = new File("target/jacoco.exec");
			System.out.println("Writing data to " + file.getAbsolutePath());
			final FileOutputStream fileStream = new FileOutputStream(file,
					false);
			final OutputStream bufferedStream = new BufferedOutputStream(
					fileStream);
			bufferedStream.write(insiders.toByteArray());
			bufferedStream.write(outsiders);
			bufferedStream.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
