/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation;

import org.jacoco.core.internal.Java9Support;
import org.jacoco.core.test.TargetLoader;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test of
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8180660">JDK-8180660</a>.
 */
public class JDK8180660Test {

	private static final boolean isJDK;
	private static final boolean isJDK8u152;

	static {
		isJDK = Compiler.DETECT.isJDK();
		final Matcher m = Pattern.compile("1\\.8\\.0_(\\d++)(-ea)?")
				.matcher(System.getProperty("java.version"));
		isJDK8u152 = m.matches() && Integer.parseInt(m.group(1)) >= 152;
	}

	private static void target() {
		try {
			nop();
		} finally {
			nop(); // $line-finallyBlock$
		} // $line-finallyEnd$
	}

	@Test
	public void test() throws IOException {
		target();

		final Source source = Source.getSourceFor("src", JDK8180660Test.class);
		final ClassNode classNode = new ClassNode();
		new ClassReader(Java9Support.downgradeIfRequired(
				TargetLoader.getClassDataAsBytes(JDK8180660Test.class)))
						.accept(classNode, ClassReader.SKIP_FRAMES);

		MethodNode method = null;
		for (final MethodNode m : classNode.methods) {
			if ("target".equals(m.name)) {
				assertNull(method);
				method = m;
			}
		}
		assertNotNull("Target method not found", method);

		int line = -1;
		for (AbstractInsnNode i = method.instructions
				.getFirst(); i != null; i = i.getNext()) {
			if (i.getType() == AbstractInsnNode.LINE) {
				line = ((LineNumberNode) i).line;
			}
			if (isJDK && i.getOpcode() == Opcodes.GOTO) {
				assertEquals(source.getLineNumber("finallyEnd"), line);
			}
			if (i.getOpcode() == Opcodes.ATHROW) {
				if (!Compiler.DETECT.isJDK() || isJDK8u152) {
					assertEquals(source.getLineNumber("finallyEnd"), line);
				} else {
					assertEquals(source.getLineNumber("finallyBlock"), line);
				}
			}
		}
	}

}
