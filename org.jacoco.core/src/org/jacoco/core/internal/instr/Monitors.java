package org.jacoco.core.internal.instr;

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import jdk.internal.org.objectweb.asm.util.Textifier;

/**
 * https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-2.html#jvms-2.11.10
 */
public final class Monitors {

	// TODO use array instead?
	private final HashMap<AbstractInsnNode, List<TryCatchBlockNode>> handlers = new HashMap<AbstractInsnNode, List<TryCatchBlockNode>>();

	// TODO use array instead?
	private final HashMap<AbstractInsnNode, Status> statuses = new HashMap<AbstractInsnNode, Status>();

	private final Stack<AbstractInsnNode> workList = new Stack<AbstractInsnNode>();

	public static Monitors compute(MethodNode method) {
		Monitors monitors = new Monitors();
		monitors.analyze(method);
		return monitors;
	}

	public static final class Status {
		public int monitors;

		public Status(int monitors) {
			this.monitors = monitors;
		}
	}

	public void analyze(MethodNode m) {
		if (m.instructions.size() == 0) {
			return;
		}

		for (TryCatchBlockNode c : m.tryCatchBlocks) {
			for (AbstractInsnNode i = c.start; i != c.end; i = i.getNext()) {
				List<TryCatchBlockNode> instructionHandlers = handlers.get(i);
				if (instructionHandlers == null) {
					instructionHandlers = new ArrayList<TryCatchBlockNode>();
					handlers.put(i, instructionHandlers);
				}
				instructionHandlers.add(c);
			}
		}

		statuses.put(m.instructions.getFirst(), new Status(0));
		workList.push(m.instructions.getFirst());
		while (!workList.isEmpty()) {
			final AbstractInsnNode instruction = workList.pop();
			final Status status = statuses.get(instruction);
			if (status.monitors < 0) {
				// error
				continue;
			}
			if (instruction.getOpcode() == Opcodes.MONITORENTER) {
				status.monitors++;
				merge(status, instruction.getNext());
			} else if (instruction.getOpcode() == Opcodes.MONITOREXIT) {
				status.monitors--;
				merge(status, instruction.getNext());
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
					// TODO also LINE, FRAME
					mergeIntoHandlers(status, instruction);
				}
				merge(status, instruction.getNext());
			}
		}
	}

	public List<TryCatchBlockNode> getHandlers(AbstractInsnNode i) {
		final List<TryCatchBlockNode> result = handlers.get(i);
		return result != null ? result : Collections.<TryCatchBlockNode>emptyList();
	}

	/**
	 * TODO add comment about -1 and -2
	 *
	 * @return number of held monitors after execution of given instruction
	 */
	public int getMonitors(final AbstractInsnNode i) {
		final Status status = statuses.get(i);
		return status != null ? status.monitors : 0;
	}

	private void mergeIntoHandlers(Status status,
			AbstractInsnNode instruction) {
		for (TryCatchBlockNode c : getHandlers(instruction)) {
			merge(status, c.handler);
		}
	}

	private void merge(Status status, AbstractInsnNode target) {
		if (target == null) {
			// TODO instruction.getNext can be null for incorrect methods
			throw new NullPointerException();
		}
		if (statuses.get(target) == null) {
			statuses.put(target, new Status(status.monitors));
			workList.add(target);
		} else {
			final Status s = statuses.get(target);
			if (status.monitors != s.monitors) {
				s.monitors = -2;
			}
		}
	}

	void print(MethodNode m) {
		int index = 0;
		for (AbstractInsnNode i : m.instructions) {
			Monitors.Status status = statuses.get(i);
			System.out.print(index + ":");
			System.out.print(" ");
			System.out
					.print("M=" + (status == null ? "null" : status.monitors));
			System.out.print(" ");
			System.out.print("H=" + getHandlers(i).size());
			System.out.print(" ");
			if (i.getType() == AbstractInsnNode.LABEL) {
				System.out.println("L");
			} else {
				System.out.println(Textifier.OPCODES[i.getOpcode()]);
			}
			index++;
		}
		System.out.println();
	}

}
