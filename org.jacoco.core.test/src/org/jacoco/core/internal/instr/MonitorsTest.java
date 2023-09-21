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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.flow.MonitorsAnalyzerTest;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link Monitors}.
 *
 * @deprecated replace by {@link MonitorsAnalyzerTest}
 */
@org.junit.Ignore
@Deprecated
public class MonitorsTest {

	@Test
	public void monitorenter() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitInsn(Opcodes.RETURN);
		Monitors monitors = analyze(m);
		assertEquals(0, monitors.getMonitors(m.instructions.get(0)));
		assertEquals(1, monitors.getMonitors(m.instructions.get(1)));
		assertEquals("non-empty monitor stack at return", check(m, monitors));
	}

	@Test
	public void monitorexit() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		Monitors monitors = analyze(m);
		assertEquals(0, monitors.getMonitors(m.instructions.get(0)));
		assertEquals(-1, monitors.getMonitors(m.instructions.get(1)));
		assertEquals("monitor stack underflow", check(m, monitors));
	}

	@Test
	public void merge_conflict() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		Label target = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, target);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(target);
		m.visitInsn(Opcodes.RETURN);
		assertEquals("monitor stack height merge conflict",
				check(m, analyze(m)));
	}

	@Test
	public void conflict_should_propagate() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		Label label = new Label();
		m.visitLabel(label);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitJumpInsn(Opcodes.GOTO, label);
		Monitors monitors = analyze(m);
		assertEquals("monitor stack height merge conflict", check(m, monitors));
		assertEquals(Monitors.CONFLICT,
				monitors.getMonitors(m.instructions.get(1)));
	}

	@Test
	public void test() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitInsn(org.objectweb.asm.Opcodes.NOP);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		Monitors monitors = analyze(m);
		assertEquals(0, monitors.getMonitors(m.instructions.get(0)));
		assertEquals(1, monitors.getMonitors(m.instructions.get(1)));
		assertEquals(1, monitors.getMonitors(m.instructions.get(2)));
		assertEquals("ok", check(m, monitors));
	}

	@Test
	public void unhandled() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitInsn(Opcodes.BASTORE);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		assertEquals("non-empty monitor stack at exceptional exit",
				check(m, analyze(m)));
	}

	@Test
	public void test2() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		Label handler = new Label();
		Label start = new Label();
		Label end = new Label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitLabel(start);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(end);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);
		assertEquals("ok", check(m, analyze(m)));
	}

	@Test
	public void unreachable() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		m.visitInsn(Opcodes.RETURN);
		m.visitInsn(Opcodes.RETURN);
		assertEquals("ok", check(m, analyze(m)));
	}

	/**
	 * @deprecated replaced by
	 *             {@link MonitorsAnalyzerTest#method_without_instructions()}
	 */
	@Test
	public void empty() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		assertEquals("ok", check(m, analyze(m)));
	}

	@Test
	public void improper_pair() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		Label target = new Label();
		Label after = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, target);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(target);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitJumpInsn(Opcodes.GOTO, after);
		m.visitLabel(after);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.RETURN);
		assertEquals("ok", check(m, analyze(m)));
	}

	/**
	 * @deprecated replaced by {@link MonitorsAnalyzerTest#invalid()}
	 */
	@Test
	public void invalid() {
		MethodNode m = new MethodNode(0, "m", "()V", null, null);
		m.visitInsn(Opcodes.NOP);
		try {
			analyze(m);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
			// expected
		}
	}

	static boolean canThrow(final AbstractInsnNode instruction) {
		switch (instruction.getType()) {
		case AbstractInsnNode.JUMP_INSN:
		case AbstractInsnNode.LABEL:
		case AbstractInsnNode.LINE:
		case AbstractInsnNode.FRAME:
			return false;
		}
		switch (instruction.getOpcode()) {
		case Opcodes.MONITORENTER:
		case Opcodes.MONITOREXIT:
			// TODO according to JVMS can throw NPE or
			// IllegalMonitorStateException ?
		case Opcodes.NOP:
			return false;
		default:
			return true;
		}
	}

	private String check(MethodNode method, Monitors monitors) {
		for (AbstractInsnNode instruction : method.instructions) {
			int m = monitors.getMonitors(instruction);
			switch (m) {
			case 0:
			case Monitors.UNREACHABLE:
				continue;
			case Monitors.UNDERFLOW:
				return "monitor stack underflow";
			case Monitors.CONFLICT:
				return "monitor stack height merge conflict";
			}
			if (Opcodes.IRETURN <= instruction.getOpcode()
					&& instruction.getOpcode() <= Opcodes.RETURN) {
				return "non-empty monitor stack at return";
			}
			if (!canThrow(instruction)) {
				continue;
			}
			if (!monitors.hasCatchAll(instruction)) {
				return "non-empty monitor stack at exceptional exit";
			}
		}
		return "ok";
	}

	private Monitors analyze(MethodNode method) {
		return Monitors.compute(method).print();
	}

}
