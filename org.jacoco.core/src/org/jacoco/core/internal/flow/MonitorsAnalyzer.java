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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * TODO add description
 *
 * https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-2.html#jvms-2.11.10
 */
public final class MonitorsAnalyzer {

	public static boolean EVALUATE_IDEA = false;

	private final MethodNode method;

	private final MonitorsStack[] stacks;

	private final int[] catchAnyHandlers;

	public MonitorsAnalyzer(final MethodNode method) {
		this.method = method;
		this.stacks = new MonitorsStack[method.instructions.size()];
		this.catchAnyHandlers = new int[method.instructions.size()];
		analyze();
	}

	/**
	 * FIXME we need after instead of "prior" - consider jump instruction with
	 * probe right after monitorenter, actually we DO NOT NEED
	 *
	 * @return monitors stack prior to execution of given instruction
	 */
	public MonitorsStack monitorsStack(final int index) {
		return stacks[index];
	}

	/**
	 * @return monitors stack prior to execution of catch-any handler of given
	 *         instruction, or {@link #EMPTY} if no catch-any handler
	 */
	public MonitorsStack monitorsStackAtCatchAny(final int index) {
		final int handler = catchAnyHandlers[index];
		return handler == -1 ? EMPTY : monitorsStack(handler);
	}

	public static class MonitorsStack {
		private final int size;
		public final MonitorsStack previous;
		public final int var;

		private MonitorsStack(final int size) {
			this.size = size;
			this.previous = null;
			this.var = -1;
		}

		private MonitorsStack(final MonitorsStack previous, final int var) {
			this.size = previous.size + 1;
			this.previous = previous;
			this.var = var;
		}
	}

	public final static MonitorsStack EMPTY = new MonitorsStack(0);
	public final static MonitorsStack UNDERFLOW = new MonitorsStack(-1);
	public final static MonitorsStack UNREACHABLE = new MonitorsStack(-2);
	private final static MonitorsStack CONFLICT = new MonitorsStack(-3);

	private void analyze() {
		if (method.instructions.size() == 0) {
			return;
		}
		final ArrayList<AbstractInsnNode>[] handlersByInstructionIndex = new ArrayList[method.instructions
				.size()];
		Arrays.fill(catchAnyHandlers, -1);
		for (final TryCatchBlockNode c : method.tryCatchBlocks) {
			final int startIndex = method.instructions.indexOf(c.start);
			final int endIndex = method.instructions.indexOf(c.end);
			for (int i = startIndex; i < endIndex; i++) {
				ArrayList<AbstractInsnNode> instructionHandlers = handlersByInstructionIndex[i];
				if (instructionHandlers == null) {
					instructionHandlers = new ArrayList<AbstractInsnNode>();
					handlersByInstructionIndex[i] = instructionHandlers;
				}
				if (/* TODO explain this condition */catchAnyHandlers[i] == -1) {
					instructionHandlers.add(c.handler);
					if (c.type == null) {
						catchAnyHandlers[i] = method.instructions
								.indexOf(c.handler);
					}
				}
			}
		}

		Arrays.fill(stacks, UNREACHABLE);
		stacks[0] = EMPTY;

		final LinkedList<AbstractInsnNode> workList = new LinkedList<AbstractInsnNode>();
		workList.push(method.instructions.getFirst());
		while (!workList.isEmpty()) {
			final AbstractInsnNode instruction = workList.pop();
			final int instructionIndex = method.instructions
					.indexOf(instruction);
			final MonitorsStack stack = stacks[instructionIndex];
			if (stack.size < 0) {
				continue;
			}
			switch (instruction.getType()) {
			case AbstractInsnNode.LABEL:
			case AbstractInsnNode.FRAME:
			case AbstractInsnNode.LINE:
			case AbstractInsnNode.VAR_INSN:
				merge(stack, instruction.getNext(), workList);
				break;
			case AbstractInsnNode.JUMP_INSN:
				merge(stack, ((JumpInsnNode) instruction).label, workList);
				if (Opcodes.GOTO != instruction.getOpcode()) {
					merge(stack, instruction.getNext(), workList);
				}
				break;
			case AbstractInsnNode.LOOKUPSWITCH_INSN: {
				final LookupSwitchInsnNode switchInstruction = (LookupSwitchInsnNode) instruction;
				merge(stack, switchInstruction.dflt, workList);
				merge(stack, switchInstruction.labels, workList);
				break;
			}
			case AbstractInsnNode.TABLESWITCH_INSN: {
				final TableSwitchInsnNode switchInstruction = (TableSwitchInsnNode) instruction;
				merge(stack, switchInstruction.dflt, workList);
				merge(stack, switchInstruction.labels, workList);
				break;
			}
			default:
				switch (instruction.getOpcode()) {
				case Opcodes.MONITORENTER:
					// FIXME ClassCastException
					final int var = ((VarInsnNode) instruction.getPrevious()).var;
					final MonitorsStack push = new MonitorsStack(stack, var);
					merge(push, instruction.getNext(), workList);
					break;
				case Opcodes.MONITOREXIT:
					final MonitorsStack pop = stack.previous == null ? UNDERFLOW
							: stack.previous;
					merge(pop, instruction.getNext(), workList);
					break;
				case Opcodes.IRETURN:
				case Opcodes.LRETURN:
				case Opcodes.FRETURN:
				case Opcodes.DRETURN:
				case Opcodes.ARETURN:
				case Opcodes.RETURN:
					break;
				case Opcodes.ATHROW:
					merge(stack, handlersByInstructionIndex[instructionIndex],
							workList);
					break;
				default:
					merge(stack, handlersByInstructionIndex[instructionIndex],
							workList);
					merge(stack, instruction.getNext(), workList);
					break;
				}
			}
		}
	}

	private void merge(final MonitorsStack stack,
			final List<? extends AbstractInsnNode> targets,
			final LinkedList<AbstractInsnNode> workList) {
		if (targets == null) {
			return;
		}
		for (final AbstractInsnNode target : targets) {
			merge(stack, target, workList);
		}
	}

	private void merge(final MonitorsStack stack, final AbstractInsnNode target,
			final LinkedList<AbstractInsnNode> workList) {
		if (target == null) {
			return;
		}
		final int targetIndex = method.instructions.indexOf(target);
		if (stacks[targetIndex] == UNREACHABLE) {
			stacks[targetIndex] = stack;
			workList.add(target);
		} else {
			final MonitorsStack targetStack = stacks[targetIndex];
			if (stack != targetStack) {
				stacks[targetIndex] = CONFLICT;
			}
		}
	}
}
