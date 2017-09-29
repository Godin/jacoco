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
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class FinallyFilterTest implements IFilterOutput {

	private final FinallyFilter filter = new FinallyFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	// === try catch finally ===

	@Test
	public void javac_try_catch_finally() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);
		m.visitTryCatchBlock(catchStart, catchEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		// has line number of a closing curly brace of a finally block:
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(catchStart);
		m.visitInsn(Opcodes.NOP); // catch body
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		// has line number of a closing curly brace of a finally block:
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(finallyStart);
		// has line number of a first instruction of a finally block:
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitVarInsn(Opcodes.ALOAD, 1);
		// has line number of a last instruction of a finally block:
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP);

		execute();
	}

	@Test
	public void ecj_try_catch_finally() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();
		final Label after = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, catchEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);
		m.visitLabel(tryEnd);

		m.visitLabel(catchStart);
		m.visitInsn(Opcodes.NOP); // catch body
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, after);

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP); // finally body
		m.visitLabel(after);

		m.visitInsn(Opcodes.NOP);

		execute();
	}

	// === always completes abruptly ===

	@Test
	public void javac_always_completes_abruptly() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label finallyStart = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.RETURN); // finally body

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.RETURN); // finally body

		execute();
	}

	@Test
	public void ecj_always_completes_abruptly() {
		final Label tryStart = new Label();
		final Label finallyStart = new Label();
		final Label f = new Label();

		m.visitTryCatchBlock(tryStart, finallyStart, f, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitJumpInsn(Opcodes.GOTO, f);

		m.visitLabel(finallyStart);
		m.visitInsn(Opcodes.POP);
		m.visitLabel(f);
		m.visitInsn(Opcodes.RETURN); // finally body

		execute();
	}

	// === empty catch ===

	/**
	 * javac 1.5 - 1.7
	 */
	@Test
	public void javac_try_empty_catch_finally() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label catchEnd = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);
		m.visitTryCatchBlock(catchStart, catchEnd, finallyStart, null);
		// TODO actually there is one more TryCatchBlock for ASTORE in finally

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(catchStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitLabel(catchEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP);

		execute();
	}

	/**
	 * javac 1.8
	 */
	@Test
	public void javac_8_try_empty_catch_finally() {
		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label catchStart = new Label();
		final Label finallyStart = new Label();
		final Label finallyEnd = new Label();

		m.visitTryCatchBlock(tryStart, tryEnd, catchStart,
				"java/lang/Exception");
		m.visitTryCatchBlock(tryStart, tryEnd, finallyStart, null);

		m.visitLabel(tryStart);
		m.visitInsn(Opcodes.NOP); // try body
		m.visitLabel(tryEnd);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(catchStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.NOP); // finally body
		m.visitJumpInsn(Opcodes.GOTO, finallyEnd);

		m.visitLabel(finallyStart);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.NOP); // finally body
		expectedMerged.add(m.instructions.getLast());
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(finallyEnd);

		m.visitInsn(Opcodes.NOP);

		execute();
	}

	// public static void main(String[] args) throws Exception {
	// Textifier.main(new String[] { "/tmp/j/T.class" });
	// }

	private void execute() {
		filter.filter("", "", m, this);
		assertEquals(expectedMerged, actualMerged);
		assertEquals(expectedIgnored, actualIgnored);
	}

	private final Set<AbstractInsnNode> expectedMerged = new HashSet<>();
	private final Set<AbstractInsnNode> expectedIgnored = new HashSet<>();
	private final Set<AbstractInsnNode> actualMerged = new HashSet<>();
	private final Set<AbstractInsnNode> actualIgnored = new HashSet<>();

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
				.getNext()) {
			actualIgnored.add(i);
		}
		actualIgnored.add(toInclusive);
	}

	public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
		if (actualMerged.isEmpty() || actualMerged.contains(i1)
				|| actualMerged.contains(i2)) {
			actualMerged.add(i1);
			actualMerged.add(i2);
		} else {
			fail();
		}
	}

}
