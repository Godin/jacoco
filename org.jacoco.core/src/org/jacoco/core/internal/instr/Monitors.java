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

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * <blockquote>
 * <p>
 * Structured locking is the situation when, during a method invocation, every
 * exit on a given monitor matches a preceding entry on that monitor.
 * </p>
 * </blockquote>
 *
 * @deprecated replace by {@link org.jacoco.core.internal.flow.MonitorsAnalyzer}
 */
public final class Monitors {

	public final MethodNode method;

	private final List<TryCatchBlockNode>[] handlers;

	public final AbstractInsnNode[] catchAllHandler;

	public static final int UNREACHABLE = -3;
	public static final int CONFLICT = -2;
	public static final int UNDERFLOW = -1;
	private static final MonitorsStack EMPTY_STACK = new MonitorsStack(0, null);
	private static final MonitorsStack UNREACHABLE_STACK = new MonitorsStack(
			UNREACHABLE, null);
	private static final MonitorsStack CONFLICT_STACK = new MonitorsStack(
			CONFLICT, null);
	private static final MonitorsStack UNDERFLOW_STACK = new MonitorsStack(
			UNDERFLOW, null);

	public boolean same(int i1, AbstractInsnNode i2) {
		return stacks[i1] == stacks[method.instructions.indexOf(i2)];
	}

	private static class MonitorsStack {
		private final int size;
		private final MonitorsStack previous;

		MonitorsStack(final int size, final MonitorsStack previous) {
			this.size = size;
			this.previous = previous;
		}

		MonitorsStack pop() {
			return this == EMPTY_STACK ? UNDERFLOW_STACK : previous;
		}

		MonitorsStack push() {
			return new MonitorsStack(this.size + 1, this);
		}
	}

	private final MonitorsStack[] stacks;

	// TODO move into method
	private final Stack<AbstractInsnNode> workList = new Stack<AbstractInsnNode>();

	public Monitors(final MethodNode method) {
		this.method = method;
		this.handlers = new List[method.instructions.size()];
		this.catchAllHandler = new AbstractInsnNode[method.instructions.size()];
		this.stacks = new MonitorsStack[method.instructions.size()];
		Arrays.fill(stacks, UNREACHABLE_STACK);
	}

	public static Monitors compute(final MethodNode method) {
		Monitors monitors = new Monitors(method);
		monitors.analyze();
		return monitors;
	}

	private void analyze() {
		if (method.instructions.size() == 0) {
			return;
		}

		for (final TryCatchBlockNode c : method.tryCatchBlocks) {
			final int startIndex = method.instructions.indexOf(c.start);
			final int endIndex = method.instructions.indexOf(c.end);
			// TODO or i <= endIndex ?
			for (int i = startIndex; i < endIndex; i++) {
				List<TryCatchBlockNode> instructionHandlers = handlers[i];
				if (instructionHandlers == null) {
					instructionHandlers = new ArrayList<TryCatchBlockNode>();
					handlers[i] = instructionHandlers;
				}
				if (/* TODO explain this condition */ catchAllHandler[i] == null) {
					instructionHandlers.add(c);
					if (c.type == null) {
						catchAllHandler[i] = c.handler;
					}
				}
			}
		}

		stacks[0] = EMPTY_STACK;
		workList.push(method.instructions.getFirst());
		while (!workList.isEmpty()) {
			final AbstractInsnNode instruction = workList.pop();
			final int instructionIndex = method.instructions
					.indexOf(instruction);
			final MonitorsStack stack = stacks[instructionIndex];
			if (stack.size < 0) {
				// error
				continue;
			}
			if (instruction.getOpcode() == Opcodes.MONITORENTER) {
				merge(stack.push(), instruction.getNext());
			} else if (instruction.getOpcode() == Opcodes.MONITOREXIT) {
				merge(stack.pop(), instruction.getNext());
			} else if (instruction.getOpcode() == Opcodes.TABLESWITCH) {
				TableSwitchInsnNode switchInstruction = (TableSwitchInsnNode) instruction;
				merge(stack, switchInstruction.dflt);
				for (LabelNode target : switchInstruction.labels) {
					merge(stack, target);
				}
				// TODO likely wrong:
				merge(stack, instruction.getNext());
			} else if (instruction.getOpcode() == Opcodes.LOOKUPSWITCH) {
				LookupSwitchInsnNode switchInstruction = (LookupSwitchInsnNode) instruction;
				merge(stack, switchInstruction.dflt);
				for (LabelNode target : switchInstruction.labels) {
					merge(stack, target);
				}
				// TODO likely wrong:
				merge(stack, instruction.getNext());
			} else if (instruction.getType() == AbstractInsnNode.JUMP_INSN) {
				if (instruction.getOpcode() != Opcodes.GOTO) {
					// TODO missing test for GOTO
					merge(stack, instruction.getNext());
				}
				merge(stack, ((JumpInsnNode) instruction).label);
			} else if (instruction.getOpcode() == Opcodes.ATHROW) {
				mergeIntoHandlers(stack, instruction);
			} else if (Opcodes.IRETURN <= instruction.getOpcode()
					&& instruction.getOpcode() <= Opcodes.RETURN) {
				// nothing to do
			} else {
				// FIXME exclude non-throwing instructions such as NOP,
				// ALOAD/ASTORE, JUMPs
				if (AbstractInsnNode.LABEL != instruction.getType()
						&& AbstractInsnNode.FRAME != instruction.getType()) {
					// See doExceptionEdge in
					// https://github.com/openjdk/jdk/blob/486fa08d4b22243443d39efa34c78d7e9eb44775/src/jdk.hotspot.agent/share/classes/sun/jvm/hotspot/oops/GenerateOopMap.java#L1434
					// TODO also LINE, FRAME ?
					mergeIntoHandlers(stack, instruction);
				}
				merge(stack, instruction.getNext());
			}
		}
	}

	public List<TryCatchBlockNode> getHandlers(final AbstractInsnNode i) {
		final List<TryCatchBlockNode> handlers = this.handlers[method.instructions
				.indexOf(i)];
		return handlers != null ? handlers
				: Collections.<TryCatchBlockNode> emptyList();
	}

	public boolean hasCatchAll(final AbstractInsnNode i) {
		return catchAllHandler[method.instructions.indexOf(i)] != null;
	}

	/**
	 * TODO add comment about -1, -2 and -3
	 *
	 * FIXME we need after instead of "prior" - consider jump instruction with
	 * probe right after monitorenter
	 *
	 * @return number of held monitors prior to execution of given instruction
	 */
	public int getMonitors(final AbstractInsnNode i) {
		return stacks[method.instructions.indexOf(i)].size;
	}

	public int getMonitors(final int index) {
		return stacks[index].size;
	}

	private void mergeIntoHandlers(final MonitorsStack stack,
			final AbstractInsnNode instruction) {
		for (final TryCatchBlockNode c : getHandlers(instruction)) {
			merge(stack, c.handler);
		}
	}

	private void merge(final MonitorsStack stack,
			final AbstractInsnNode target) {
		if (target == null) {
			// TODO instruction.getNext can be null for incorrect methods
			throw new NullPointerException();
		}
		final int targetIndex = method.instructions.indexOf(target);
		if (this.stacks[targetIndex] == UNREACHABLE_STACK) {
			this.stacks[targetIndex] = stack;
			workList.add(target);
		} else {
			final MonitorsStack targetStack = this.stacks[targetIndex];
			// TODO is reference comparison ok?
			if (stack != targetStack) {
				// TODO conflict should propagate
				this.stacks[targetIndex] = CONFLICT_STACK;
			}
		}
	}

	@Deprecated
	public Monitors print() {
		System.out.println(method.name);
		int index = 0;
		for (AbstractInsnNode i : method.instructions) {
			System.out.print(index + ":");
			System.out.print(" ");
			System.out.print("M=" + getMonitors(i));
			System.out.print(" ");
			System.out.print(
					"H=" + Arrays.toString(convertHandlers(getHandlers(i))));
			System.out.print(" ");
			System.out.print("P=" + (catchAllHandler[index] != null));
			System.out.print(" ");
			if (i.getType() == AbstractInsnNode.LABEL) {
				System.out.print("L");
			} else if (i.getType() == AbstractInsnNode.FRAME
					|| i.getType() == AbstractInsnNode.LINE) {
			} else {
				// System.out.print(Textifier.OPCODES[i.getOpcode()]);
				if (i.getType() == AbstractInsnNode.JUMP_INSN) {
					System.out.print(" " + indexOf(((JumpInsnNode) i).label));
				}
			}
			System.out.println();
			index++;
		}
		System.out.println();
		return this;
	}

	private int indexOf(AbstractInsnNode i) {
		return method.instructions.indexOf(i);
	}

	private int[] convertHandlers(List<TryCatchBlockNode> handlers) {
		int[] h = new int[handlers.size()];
		for (int i = 0; i < h.length; i++) {
			h[i] = method.instructions.indexOf(handlers.get(i).handler);
		}
		return h;
	}

}
