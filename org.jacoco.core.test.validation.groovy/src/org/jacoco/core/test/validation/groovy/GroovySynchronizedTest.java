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
package org.jacoco.core.test.validation.groovy;

import java.io.IOException;

import org.jacoco.core.internal.flow.AsmUtils;
import org.jacoco.core.test.Jit;
import org.jacoco.core.test.JvmProcess;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.groovy.targets.GroovySynchronizedTarget;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test of code coverage in {@link GroovySynchronizedTarget}.
 */
public class GroovySynchronizedTest extends ValidationTestBase {

	public GroovySynchronizedTest() {
		super(GroovySynchronizedTarget.class);
	}

	@Test
	public void print() throws IOException {
		System.out.println(AsmUtils.instrumentToString( //
				AsmUtils.classBytesToClassNode( //
						TargetLoader.getClassDataAsBytes( //
								GroovySynchronizedTarget.class //
						))));
	}

	@Override
	public void all_missed_instructions_should_have_line_number() {
	}

	/**
	 * FIXME this test is quite long due to "-Xcomp" ~8sec compileonly ~2.5sec
	 */
	@Test
	public void testGroovy() throws Exception {
		final byte[] classBytes = TargetLoader
				.getClassDataAsBytes(GroovySynchronizedTarget.class);
		final JvmProcess jvmProcess = new JvmProcess() //
				.addOption("-Xcomp") //
				// .addOption("-XX:+PrintWarnings") //
				// NOTE: -XX:+PrintCompilation together with CompileCommand
				// option
				// PrintCompilation reduce list of messages about
				// compilation
				// .addOption("-XX:+PrintCompilation") //
				// .addOption(
				// "-XX:CompileCommand=option "
				// + GroovySynchronizedTarget.class.getName()
				// .replace('.', '/')
				// + "*.*" + ",PrintCompilation")
				// .addOption("-XX:CompileCommand=help") //
				.addOption("-XX:CompileCommand=quiet")
				.addOption("-XX:CompileCommand=compileonly "
						+ GroovySynchronizedTarget.class.getName().replace('.',
								'/')
						+ "*.*") //
		; //
		Assert.assertEquals("original", "", jvmProcess
				.execute(GroovySynchronizedTarget.class.getName(), classBytes));
		Assert.assertEquals("instrumented", "",
				jvmProcess.execute(GroovySynchronizedTarget.class.getName(),
						Jit.instrument(classBytes)));
	}

}
