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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Filters duplicates of finally blocks that compiler generates.
 */
public final class FinallyFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		for (AbstractInsnNode i = methodNode.instructions.getFirst(); i != null; i = i.getNext()) {
			System.out.println(i);
		}

		for (final TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
			if (tryCatchBlock.type == null) {
				filter(methodNode, tryCatchBlock, output);
			}
		}
	}

	static class W {
		final MethodNode m;
		final int start;
		final int end;
		final Set<AbstractInsnNode> visited = new HashSet<AbstractInsnNode>();
		final Set<AbstractInsnNode> r = new HashSet<AbstractInsnNode>();

		public W(final MethodNode methodNode, TryCatchBlockNode n) {
			this.m = methodNode;
			this.start = m.instructions.indexOf(n.start);
			this.end = m.instructions.indexOf(n.end);
		}

		void visit() {
			for (TryCatchBlockNode tryCatchBlock : m.tryCatchBlocks) {
				if (start <= m.instructions.indexOf(tryCatchBlock.start) && m.instructions.indexOf(tryCatchBlock.end) <= end) {
					if (tryCatchBlock.type != null) {
						visit(tryCatchBlock.handler);
					} else {
//						visit(tryCatchBlock.start);
					}
				}
			}
		}

		void visit(AbstractInsnNode i) {
			if (!visited.add(i)) {
				return;
			}
			if (i == null) {
				return;
			}
			final int index = m.instructions.indexOf(i);
			if (index > end) {
				r.add(i);
				return;
			}
			if (AbstractInsnNode.JUMP_INSN == i.getType()) {
				visit(((JumpInsnNode) i).label);
				if (i.getOpcode() != Opcodes.GOTO) {
					visit(i.getNext());
				}
			} else {
				visit(i.getNext());
			}
		}
	}

	private void filter(final MethodNode m, final TryCatchBlockNode tryCatchBlock,
			final IFilterOutput output) {
		AbstractInsnNode e = next(tryCatchBlock.handler);
		AbstractInsnNode n = next(tryCatchBlock.end);
		final int size = size(e);
		if (size > 0 && isSame(size, next(e), n)) {

			W w = new W(m, tryCatchBlock);
			w.visit();
			for (AbstractInsnNode r : w.r) {
				output.ignore(r, r);
			}

			output.ignore(e, e);
			e = next(e);
			for (int i = 0; i < size; i++) {
				output.merge(e, n);
				e = next(e);
				n = next(n);
			}
			output.ignore(e, next(e));
		}
	}

	private static boolean isSame(final int size, AbstractInsnNode e,
			AbstractInsnNode n) {
		for (int i = 0; i < size; i++) {
			if (e == null || n == null || e.getOpcode() != n.getOpcode()) {
				return false;
			}
			e = next(e);
			n = next(n);
		}
		return true;
	}

	/**
	 * @return number of instructions inside given finally handler
	 */
	private static int size(AbstractInsnNode i) {
		if (Opcodes.ASTORE != i.getOpcode()) {
			return 0;
		}
		final int var = ((VarInsnNode) i).var;
		int size = -1;
		do {
			size++;
			i = next(i);
			if (i == null) {
				return 0;
			}
		} while (!(Opcodes.ALOAD == i.getOpcode()
				&& ((VarInsnNode) i).var == var));
		i = next(i);
		if (Opcodes.ATHROW != i.getOpcode()) {
			return 0;
		}
		return size;
	}

	private static AbstractInsnNode next(AbstractInsnNode i) {
		do {
			i = i.getNext();
		} while (i != null && (AbstractInsnNode.FRAME == i.getType()
				|| AbstractInsnNode.LABEL == i.getType()
				|| AbstractInsnNode.LINE == i.getType()));
		return i;
	}

}
