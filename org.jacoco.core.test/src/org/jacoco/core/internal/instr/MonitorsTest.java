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

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Unit test for {@link Monitors}.
 */
public class MonitorsTest {

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
		final Monitors monitors = new Monitors(m);
		Assert.assertNotEquals("between", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(between)));
		Assert.assertEquals("after", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(handler)));
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
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after", Monitors.UNDERFLOW,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(handler)));
	}

	@Test
	public void method_without_instructions() {
		new Monitors(m);
	}

	@Test
	public void invalid() {
		m.visitInsn(Opcodes.NOP);
		new Monitors(m);
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
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(handler)));
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
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(handler)));
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
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after",
				monitors.stackAt(m.instructions.indexOf(after)),
				Monitors.UNREACHABLE);
		Assert.assertEquals("handler", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(handler)));
		Assert.assertEquals("target", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(target)));
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
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(handler)));
		Assert.assertEquals("target", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(target)));
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
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(handler)));
		Assert.assertEquals("default target", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(defaultTarget)));
		Assert.assertEquals("target", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(target)));
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
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.UNREACHABLE,
				monitors.stackAt(m.instructions.indexOf(handler)));
		Assert.assertEquals("default target", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(defaultTarget)));
		Assert.assertEquals("target", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(target)));
	}

	@Test
	public void instruction_throwing() {
		final LabelNode before = new LabelNode(new Label());
		final LabelNode after = new LabelNode(new Label());
		final LabelNode handler = new LabelNode(new Label());
		m.tryCatchBlocks
				.add(new TryCatchBlockNode(before, after, handler, null));
		m.instructions.add(before);
		m.visitInsn(Opcodes.ARRAYLENGTH);
		m.instructions.add(after);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(handler);
		final Monitors monitors = new Monitors(m);
		Assert.assertEquals("after", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(after)));
		Assert.assertEquals("handler", Monitors.EMPTY,
				monitors.stackAt(m.instructions.indexOf(handler)));
	}

	/**
	 * TODO improper handling of nesting manifests in nested-synchronized
	 * 
	 * here improper handling means that outer handler should not be consulted
	 * for instructions covered by inner handler
	 *
	 * FIXME add validation test for nested-synchronized
	 */
	@Test
	public void nested_try() {
		final LabelNode innerStart = new LabelNode(new Label());
		final LabelNode innerEnd = new LabelNode(new Label());
		final LabelNode innerHandler = new LabelNode(new Label());
		final LabelNode innerInside = new LabelNode(new Label());
		final LabelNode outerStart = new LabelNode(new Label());
		final LabelNode outerEnd = new LabelNode(new Label());
		final LabelNode outerHandler = new LabelNode(new Label());
		m.tryCatchBlocks.add(new TryCatchBlockNode(innerStart, innerEnd,
				innerHandler, null));
		m.tryCatchBlocks.add(new TryCatchBlockNode(outerStart, outerEnd,
				outerHandler, null));
		// TODO add some instructions between labels?
		// eg throwing into inner, non-throwing into outer and check that there
		// is flow into innerHandler but not into outerHandler
		m.instructions.add(outerStart);
		m.visitInsn(Opcodes.MONITORENTER);
		m.instructions.add(innerStart);
		m.visitInsn(Opcodes.MONITORENTER);
		m.instructions.add(innerInside);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.instructions.add(innerEnd);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.instructions.add(outerEnd);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(innerHandler);
		m.visitInsn(Opcodes.RETURN);
		m.instructions.add(outerHandler);
		m.visitInsn(Opcodes.RETURN);
		final Monitors monitors = new Monitors(m);
		final int innerHandlerIndex = m.instructions.indexOf(innerHandler);
		Assert.assertEquals(innerHandlerIndex, //
				monitors.handlersAt(m.instructions.indexOf(innerStart))
						.index());
		Assert.assertEquals(innerHandlerIndex, //
				monitors.handlersAt(m.instructions.indexOf(innerInside))
						.index());

		Assert.assertEquals(
				monitors.stackAt(m.instructions.indexOf(innerInside)),
				monitors.stackAt(innerHandlerIndex));
	}

	@Test
	public void instruction_jsr() {
		m.visitInsn(Opcodes.JSR);
		try {
			new Monitors(m);
			Assert.fail("exception expected");
		} catch (Exception e) {
			// expected
		}
	}

	@Test
	public void instruction_ret() {
		m.visitInsn(Opcodes.RET);
		try {
			new Monitors(m);
			Assert.fail("exception expected");
		} catch (Exception e) {
			// expected
		}
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
	@org.junit.Ignore
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
		final Monitors monitors = new Monitors(m);
		// FIXME actually handler unreachable
		Assert.assertEquals(monitors.stackAt(m.instructions.indexOf(start)),
				monitors.stackAt(m.instructions.indexOf(handlerStart)));
	}

}
