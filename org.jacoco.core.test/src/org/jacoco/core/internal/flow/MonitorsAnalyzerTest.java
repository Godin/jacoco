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
package org.jacoco.core.internal.flow;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Unit test for {@link MonitorsAnalyzer}.
 */
public class MonitorsAnalyzerTest {

	private final MethodNode m = new MethodNode(0, "m", "()V", null, null);

	@Test
	public void instruction_monitorenter_monitorexit() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode between = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.visitInsn(Opcodes.MONITORENTER);
		m.instructions.add(between);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.instructions.add(after);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(handler);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertNotEquals("between", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(between)));
		Assert.assertEquals("after", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
	}

	@Test
	public void instruction_monitorexit_underflow() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.instructions.add(after);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(handler);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals("after", MonitorsAnalyzer.UNDERFLOW,
				monitors.monitorsStack(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
	}

	@Test
	public void method_without_instructions() {
		new MonitorsAnalyzer(m);
	}

	@Test
	public void invalid() {
		m.visitInsn(Opcodes.NOP);
		new MonitorsAnalyzer(m);
	}

	@Test
	public void instruction_return() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(after);
		m.instructions.add(handler);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals("after", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
	}

	@Test
	public void instruction_athrow() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.visitInsn(Opcodes.ATHROW);
		m.instructions.add(after);
		m.instructions.add(handler);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals("after", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
	}

	@Test
	public void instruction_goto() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		final LabelNode target = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.instructions.add(new JumpInsnNode(Opcodes.GOTO, target));
		m.instructions.add(after);
		m.instructions.add(handler);
		m.instructions.add(target);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals("after",
				monitors.monitorsStack(m.instructions.indexOf(after)),
				MonitorsAnalyzer.UNREACHABLE);
		Assert.assertEquals("handler", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
		Assert.assertEquals("target", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(target)));
	}

	@Test
	public void instruction_jump() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		final LabelNode target = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.instructions.add(new JumpInsnNode(Opcodes.IFNULL, target));
		m.instructions.add(after);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(handler);
		m.instructions.add(target);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals("after", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
		Assert.assertEquals("target", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(target)));
	}

	@Test
	public void instruction_tableswitch() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		final LabelNode defaultTarget = new LabelNode(new Label());
		final LabelNode target = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.instructions
				.add(new TableSwitchInsnNode(0, 0, defaultTarget, target));
		m.instructions.add(after);
		m.instructions.add(handler);
		m.instructions.add(defaultTarget);
		m.instructions.add(target);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals("after", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
		Assert.assertEquals("default target", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(defaultTarget)));
		Assert.assertEquals("target", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(target)));
	}

	@Test
	public void instruction_lookupswitch() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		final LabelNode defaultTarget = new LabelNode(new Label());
		final LabelNode target = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.instructions.add(new LookupSwitchInsnNode(defaultTarget,
				new int[] { 0 }, new LabelNode[] { target }));
		m.instructions.add(after);
		m.instructions.add(handler);
		m.instructions.add(defaultTarget);
		m.instructions.add(target);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals("after", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", MonitorsAnalyzer.UNREACHABLE,
				monitors.monitorsStack(m.instructions.indexOf(handler)));
		Assert.assertEquals("default target", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(defaultTarget)));
		Assert.assertEquals("target", MonitorsAnalyzer.EMPTY,
				monitors.monitorsStack(m.instructions.indexOf(target)));
	}

	/**
	 * TODO not sure this test belongs there
	 *
	 * <code><pre>
	 * void example() {
	 *   synchronized (this) {
	 *     return;
	 *   }
	 * }
	 * </pre></code>
	 */
	@Test
	public void real() {
		final LabelNode start = new LabelNode(new Label());
		final LabelNode end = new LabelNode(new Label());
		final LabelNode handlerStart = new LabelNode(new Label());
		final LabelNode handlerEnd = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(start, end, handlerStart, null));
		m.tryCatchBlocks.add(new TryCatchBlockNode(handlerStart, handlerEnd,
				handlerStart, null));
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.instructions.add(start);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.instructions.add(end);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(handlerStart);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.instructions.add(handlerEnd);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitInsn(Opcodes.ATHROW);
		final MonitorsAnalyzer monitors = new MonitorsAnalyzer(m);
		Assert.assertEquals(
				monitors.monitorsStack(m.instructions.indexOf(start)),
				monitors.monitorsStack(m.instructions.indexOf(handlerStart)));
	}

}
