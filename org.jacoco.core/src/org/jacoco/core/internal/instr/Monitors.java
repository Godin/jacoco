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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Provides information about monitor stacks and exception handlers of a method.
 *
 * TODO add description
 */
public final class Monitors {

	private final MethodNode method;

	private final Stack[] stacks;

	private final Handler[] handlers;

	public Monitors(final MethodNode method) {
		this.method = method;
		this.stacks = new Stack[method.instructions.size()];
		this.handlers = new Handler[method.instructions.size()];
		analyze();
	}

	/**
	 * FIXME we need after instead of "prior" - consider jump instruction with
	 * probe right after monitorenter, actually we DO NOT NEED because
	 * MethodInstrumenter looks at stack of jump instruction
	 *
	 * @return monitors stack prior to execution of given instruction
	 */
	public Stack stackAt(final int index) {
		return stacks[index];
	}

	/**
	 * @return exception handlers whose range covers given instruction or
	 *         <code>null</code>
	 */
	public Handler handlersAt(final int index) {
		return handlers[index];
	}

	public static class Stack {
		private final int size;
		private final Stack previous;

		private Stack(final int size) {
			this.size = size;
			this.previous = null;
		}

		private Stack(final Stack previous) {
			this.size = previous.size + 1;
			this.previous = previous;
		}

	}

	public static class Handler {
		private final Handler previous;
		private final int index;
		private final String type;

		private Handler(final Handler previous, final int index,
				final String type) {
			this.previous = previous;
			this.index = index;
			this.type = type;
		}

		public Handler previous() {
			return previous;
		}

		/**
		 * @return index of a first instruction of this handler
		 */
		public int index() {
			return index;
		}

		public String type() {
			return type;
		}
	}

	public final static Stack EMPTY = new Stack(0);
	public final static Stack UNDERFLOW = new Stack(-1);
	// TODO what if we disagree with JVM about reachability?
	public final static Stack UNREACHABLE = new Stack(-2);
	// TODO either original method doesn't pass JVM validation too,
	// or our implementation is disagree with JVM eg in canThrow
	private final static Stack CONFLICT = new Stack(-3);

	private void analyze() {
		if (method.instructions.size() == 0) {
			return;
		}

		for (final TryCatchBlockNode c : method.tryCatchBlocks) {
			final int startIndex = method.instructions.indexOf(c.start);
			final int endIndex = method.instructions.indexOf(c.end);
			// FIXME
			// i < endIndex plays role in visitProbe that is invoked for label
			// must add a unit test to MonitorsTest
			for (int i = startIndex; i < endIndex; i++) {
				if (handlers[i] == null || handlers[i].type != null) {
					handlers[i] = new Handler(handlers[i],
							method.instructions.indexOf(c.handler), c.type);
				}
			}
		}

		Arrays.fill(stacks, UNREACHABLE);
		stacks[0] = EMPTY;
		// TODO replace LinkedList by int[]
		final LinkedList<AbstractInsnNode> workList = new LinkedList<AbstractInsnNode>();
		workList.push(method.instructions.getFirst());
		while (!workList.isEmpty()) {
			final AbstractInsnNode instruction = workList.pop();
			final int instructionIndex = method.instructions
					.indexOf(instruction);
			final Stack stack = stacks[instructionIndex];
			if (stack.size < 0) {
				continue;
			}
			switch (instruction.getType()) {
			case AbstractInsnNode.JUMP_INSN: {
				final LabelNode target = ((JumpInsnNode) instruction).label;
				merge(stack, target, workList);
				if (Opcodes.GOTO != instruction.getOpcode()) {
					merge(stack, instruction.getNext(), workList);
				}
				break;
			}
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
					final Stack push = new Stack(stack);
					merge(push, instruction.getNext(), workList);
					break;
				case Opcodes.MONITOREXIT:
					final Stack pop = stack.previous == null ? UNDERFLOW
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
					merge(stack, handlers[instructionIndex], workList);
					break;
				case Opcodes.JSR:
				case Opcodes.RET:
					throw new IllegalStateException();
				default:
					if (canThrow(instruction)) {
						merge(stack, handlers[instructionIndex], workList);
					}
					merge(stack, instruction.getNext(), workList);
					break;
				}
			}
		}
	}

	/**
	 * https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-6.html#jvms-6.5
	 * 
	 * https://github.com/jacoco/jacoco/wiki/ImplicitExceptions
	 *
	 * https://github.com/openjdk/jdk/blob/jdk-22%2B0/src/jdk.hotspot.agent/share/classes/sun/jvm/hotspot/oops/GenerateOopMap.java#L1433
	 *
	 * See "kThrow" in
	 * https://android.googlesource.com/platform/art/+/master/libdexfile/dex/dex_instruction_list.h
	 * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc
	 * https://android.googlesource.com/platform/art/+/refs/heads/main/runtime/verifier/method_verifier.h
	 * https://android.googlesource.com/platform/art/+/master/runtime/verifier/method_verifier.cc#3515
	 *
	 * TODO According to
	 * https://github.com/openjdk/jdk/blob/jdk-22%2B0/src/jdk.hotspot.agent/share/classes/sun/jvm/hotspot/interpreter/Bytecodes.java#L588
	 * LDC can trap
	 */
	private boolean canThrow(final AbstractInsnNode instruction) {
		switch (instruction.getOpcode()) {
		case Opcodes.AALOAD:
		case Opcodes.AASTORE:
		case Opcodes.ANEWARRAY:
		case Opcodes.ARRAYLENGTH:
		case Opcodes.ATHROW:
		case Opcodes.BALOAD:
		case Opcodes.BASTORE:
		case Opcodes.CALOAD:
		case Opcodes.CASTORE:
		case Opcodes.CHECKCAST:
		case Opcodes.DALOAD:
		case Opcodes.DASTORE:
		case Opcodes.FALOAD:
		case Opcodes.FASTORE:
		case Opcodes.GETFIELD:
		case Opcodes.GETSTATIC:
		case Opcodes.IALOAD:
		case Opcodes.IASTORE:
		case Opcodes.IDIV:
		case Opcodes.INVOKEDYNAMIC:
		case Opcodes.INVOKEINTERFACE:
		case Opcodes.INVOKESPECIAL:
		case Opcodes.INVOKESTATIC:
		case Opcodes.INVOKEVIRTUAL:
		case Opcodes.IREM:
		case Opcodes.LALOAD:
		case Opcodes.LASTORE:
		case Opcodes.LDIV:
		case Opcodes.LREM:
		case Opcodes.MULTIANEWARRAY:
		case Opcodes.NEW:
			// TODO NEW not in our wiki
		case Opcodes.NEWARRAY:
		case Opcodes.PUTFIELD:
		case Opcodes.SALOAD:
		case Opcodes.SASTORE:
			return true;
		default:
			return false;
		}
	}

	private void merge(final Stack stack, Handler handler,
			final LinkedList<AbstractInsnNode> workList) {
		while (handler != null) {
			merge(stack, method.instructions.get(handler.index), workList);
			handler = handler.previous;
		}
	}

	private void merge(final Stack stack,
			final List<? extends AbstractInsnNode> targets,
			final LinkedList<AbstractInsnNode> workList) {
		if (targets == null) {
			return;
		}
		for (final AbstractInsnNode target : targets) {
			merge(stack, target, workList);
		}
	}

	private void merge(final Stack stack, final AbstractInsnNode target,
			final LinkedList<AbstractInsnNode> workList) {
		if (target == null) {
			return;
		}
		final int targetIndex = method.instructions.indexOf(target);
		if (stacks[targetIndex] == UNREACHABLE) {
			stacks[targetIndex] = stack;
			workList.add(target);
		} else {
			final Stack targetStack = stacks[targetIndex];
			if (stack != targetStack) {
				stacks[targetIndex] = CONFLICT;
			}
		}
	}

}
