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
package org.jacoco.core.test.filter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.filter.targets.F;
import org.jacoco.core.test.validation.Compiler;
import org.jacoco.core.test.validation.Source;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

public class FTest {

	private static final boolean isJDKCompiler = Compiler.DETECT.isJDK();

	@Test
	public void test() throws Exception {
		verify(F.class);
	}

	private static void verify(final Class cls) throws IOException {
		final Source source = Source.getSourceFor("src", cls);
		final byte[] bytes = TargetLoader.getClassDataAsBytes(cls);
		final ClassNode classNode = new ClassNode();
		new ClassReader(bytes).accept(classNode, ClassReader.SKIP_FRAMES);

		for (final MethodNode method : classNode.methods) {
			final Checker checker = new Checker(source, method);
			for (final TryCatchBlockNode tryCatchBlock : method.tryCatchBlocks) {
				checker.check(tryCatchBlock.handler);
			}
		}
	}

	private static class Checker {
		private final Source source;
		private final Map<AbstractInsnNode, Integer> instructionToLine;

		Checker(final Source source, final MethodNode method) {
			this.source = source;
			this.instructionToLine = computeLines(method);
		}

		void check(AbstractInsnNode i) {
			i = next(i);
			if (i.getOpcode() != Opcodes.ASTORE) {
				fail("Not finally");
			}
			final String firstLine = source.getLine(instructionToLine.get(i));
			final int var = ((VarInsnNode) i).var;
			while (true) {
				i = next(i);
				if (i == null) {
					fail("Not finally");
				}
				if (Opcodes.ALOAD == i.getOpcode()
						&& ((VarInsnNode) i).var == var) {
					break;
				}
			}
			final String lastLine = source.getLine(instructionToLine.get(i));

			if (isJDKCompiler) {
				assertTrue("First: " + firstLine,
						firstLine.endsWith("finallyBlock$"));
				assertTrue("Last: " + lastLine,
						lastLine.endsWith("finallyBlock$"));
			} else {
				assertTrue("First: " + firstLine,
						firstLine.endsWith("finally$"));
				assertTrue("Last: " + lastLine,
						lastLine.endsWith("finallyEnd$"));
			}
		}

		static AbstractInsnNode next(AbstractInsnNode i) {
			do {
				i = i.getNext();
			} while (i != null && (i.getType() == AbstractInsnNode.LINE
					|| i.getType() == AbstractInsnNode.LABEL));
			return i;
		}
	}

	private static Map<AbstractInsnNode, Integer> computeLines(
			final MethodNode method) {
		final Map<AbstractInsnNode, Integer> lines = new HashMap<AbstractInsnNode, Integer>();
		int line = -1;
		for (AbstractInsnNode i = method.instructions
				.getFirst(); i != null; i = i.getNext()) {
			if (i.getType() == AbstractInsnNode.LINE) {
				line = ((LineNumberNode) i).line;
			} else {
				lines.put(i, line);
			}
		}
		return lines;
	}

}
