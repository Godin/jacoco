package org.jacoco.core.internal.instr;

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import jdk.internal.org.objectweb.asm.util.Textifier;

/**
 * https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-2.html#jvms-2.11.10
 */
public final class Monitors {

	static final int UNDERFLOW = -1;
	static final int CONFLICT = -2;
	static final int UNREACHABLE = -3;

	private final MethodNode method;

	private final List<TryCatchBlockNode>[] handlers;

	private final boolean[] catchAll;

	private final int[] monitors;

	// TODO move into method
	private final Stack<AbstractInsnNode> workList = new Stack<AbstractInsnNode>();

	public Monitors(final MethodNode method) {
		this.method = method;
		this.handlers = new List[method.instructions.size()];
		this.catchAll = new boolean[method.instructions.size()];
		this.monitors = new int[method.instructions.size()];
		Arrays.fill(monitors, UNREACHABLE);
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
				instructionHandlers.add(c);
				catchAll[i] |= c.type == null;
			}
		}

		monitors[0] = 0;
		workList.push(method.instructions.getFirst());
		while (!workList.isEmpty()) {
			final AbstractInsnNode instruction = workList.pop();
			final int instructionIndex = method.instructions
					.indexOf(instruction);
			final int status = monitors[instructionIndex];
			if (status < 0) {
				// error
				continue;
			}
			if (instruction.getOpcode() == Opcodes.MONITORENTER) {
				merge(status + 1, instruction.getNext());
			} else if (instruction.getOpcode() == Opcodes.MONITOREXIT) {
				merge(status - 1, instruction.getNext());
			} else if (instruction.getOpcode() == Opcodes.TABLESWITCH) {
				TableSwitchInsnNode switchInstruction = (TableSwitchInsnNode) instruction;
				merge(status, switchInstruction.dflt);
				for (LabelNode target : switchInstruction.labels) {
					merge(status, target);
				}
				// TODO likely wrong:
				merge(status, instruction.getNext());
			} else if (instruction.getOpcode() == Opcodes.LOOKUPSWITCH) {
				LookupSwitchInsnNode switchInstruction = (LookupSwitchInsnNode) instruction;
				merge(status, switchInstruction.dflt);
				for (LabelNode target : switchInstruction.labels) {
					merge(status, target);
				}
				// TODO likely wrong:
				merge(status, instruction.getNext());

			} else if (instruction.getType() == AbstractInsnNode.JUMP_INSN) {
				if (instruction.getOpcode() != Opcodes.GOTO) {
					// TODO missing test for GOTO
					merge(status, instruction.getNext());
				}
				merge(status, ((JumpInsnNode) instruction).label);
			} else if (instruction.getOpcode() == Opcodes.ATHROW) {
				mergeIntoHandlers(status, instruction);
			} else if (Opcodes.IRETURN <= instruction.getOpcode()
					&& instruction.getOpcode() <= Opcodes.RETURN) {
				// nothing to do
			} else {
				if (AbstractInsnNode.LABEL != instruction.getType()) {
					// See doExceptionEdge in
					// https://github.com/openjdk/jdk/blob/486fa08d4b22243443d39efa34c78d7e9eb44775/src/jdk.hotspot.agent/share/classes/sun/jvm/hotspot/oops/GenerateOopMap.java#L1434
					// TODO also LINE, FRAME ?
					mergeIntoHandlers(status, instruction);
				}
				merge(status, instruction.getNext());
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
		return catchAll[method.instructions.indexOf(i)];
	}

	/**
	 * TODO add comment about -1, -2 and -3
	 *
	 * @return number of held monitors prior to execution of given instruction
	 */
	public int getMonitors(final AbstractInsnNode i) {
		return monitors[method.instructions.indexOf(i)];
	}

	private void mergeIntoHandlers(int status, AbstractInsnNode instruction) {
		for (TryCatchBlockNode c : getHandlers(instruction)) {
			merge(status, c.handler);
		}
	}

	private void merge(final int monitors, final AbstractInsnNode target) {
		if (target == null) {
			// TODO instruction.getNext can be null for incorrect methods
			throw new NullPointerException();
		}
		final int targetIndex = method.instructions.indexOf(target);
		if (this.monitors[targetIndex] == UNREACHABLE) {
			this.monitors[targetIndex] = monitors;
			workList.add(target);
		} else {
			final int s = this.monitors[targetIndex];
			if (monitors != s) {
				this.monitors[targetIndex] = CONFLICT;
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
			System.out.print("H=" + getHandlers(i).size());
			System.out.print(" ");
			if (i.getType() == AbstractInsnNode.LABEL) {
				System.out.println("L");
			} else if (i.getType() == AbstractInsnNode.FRAME
					|| i.getType() == AbstractInsnNode.LINE) {
				System.out.println();
			} else {
				System.out.println(Textifier.OPCODES[i.getOpcode()]);
			}
			index++;
		}
		System.out.println();
		return this;
	}

}
