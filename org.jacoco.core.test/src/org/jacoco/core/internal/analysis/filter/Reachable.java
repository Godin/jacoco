/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.util.Textifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class Reachable {

	private final Stack<AbstractInsnNode> toProcess = new Stack<AbstractInsnNode>();

	private final Set<TryCatchBlockNode> tryCatchBlocks = new HashSet<TryCatchBlockNode>();

	private final Set<AbstractInsnNode> reachable = new HashSet<AbstractInsnNode>();

	private Reachable() {
	}

	public static void check(final ClassNode c) {
		for (MethodNode m : c.methods) {
			final Set<AbstractInsnNode> reachable = compute(m);

			for (int i = 0; i < m.instructions.size(); i++) {
				final AbstractInsnNode instruction = m.instructions.get(i);
				switch (instruction.getType()) {
				case AbstractInsnNode.LINE:
				case AbstractInsnNode.FRAME:
				case AbstractInsnNode.LABEL:
					break;
				default:
					if (!reachable.contains(instruction)) {
						System.out.println("Unreachable "
								+ Textifier.OPCODES[instruction.getOpcode()]
								+ " in " + c.name + "." + m.name);
					}
				}
			}
		}
	}

	static Set<AbstractInsnNode> compute(final MethodNode m) {
		return new Reachable().analyze(m);
	}

	private Set<AbstractInsnNode> analyze(MethodNode m) {
		if (m.instructions.size() == 0) {
			return Collections.emptySet();
		}

		tryCatchBlocks.addAll(m.tryCatchBlocks);

		schedule(m.instructions.getFirst());

		while (!toProcess.isEmpty()) {
			while (!toProcess.isEmpty()) {
				final AbstractInsnNode i = toProcess.pop();

				switch (i.getType()) {
				case AbstractInsnNode.TABLESWITCH_INSN:
					final TableSwitchInsnNode tableSwitch = (TableSwitchInsnNode) i;
					schedule(tableSwitch.dflt);
					for (AbstractInsnNode label : tableSwitch.labels) {
						schedule(label);
					}
					break;
				case AbstractInsnNode.LOOKUPSWITCH_INSN:
					final LookupSwitchInsnNode lookupSwitch = (LookupSwitchInsnNode) i;
					schedule(lookupSwitch.dflt);
					for (AbstractInsnNode label : lookupSwitch.labels) {
						schedule(label);
					}
					break;
				case AbstractInsnNode.JUMP_INSN:
					schedule(((JumpInsnNode) i).label);
					if (Opcodes.GOTO != i.getOpcode()) {
						schedule(i.getNext());
					}
					break;
				default:
					switch (i.getOpcode()) {
					case Opcodes.IRETURN:
					case Opcodes.LRETURN:
					case Opcodes.FRETURN:
					case Opcodes.DRETURN:
					case Opcodes.ARETURN:
					case Opcodes.RETURN:
					case Opcodes.ATHROW:
						break;
					default:
						schedule(i.getNext());
						break;
					}
				}
			}

			scheduleExceptionHandlers();
		}

		return reachable;
	}

	private void schedule(AbstractInsnNode i) {
		if (!reachable.contains(i)) {
			reachable.add(i);
			toProcess.push(i);
		}
	}

	private void scheduleExceptionHandlers() {
		final Iterator<TryCatchBlockNode> i = tryCatchBlocks.iterator();
		while (i.hasNext()) {
			final TryCatchBlockNode t = i.next();
			if (reachable.contains(t.handler)) {
				i.remove();
				continue;
			}
			for (AbstractInsnNode r = t.start; r != t.end; r = r.getNext()) {
				if (reachable.contains(r)) {
					reachable.add(t.handler);
					toProcess.add(t.handler);
					i.remove();
					break;
				}
			}
		}
	}

}
