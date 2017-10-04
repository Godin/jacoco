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

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters duplicates of finally blocks that compiler generates.
 */
public final class FinallyFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		for (final TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
			if (tryCatchBlock.type == null) {
				new Matcher(output, methodNode, tryCatchBlock).filter();
			}
		}
	}

	private static class Matcher {
		final IFilterOutput output;
		final MethodNode m;
		final TryCatchBlockNode finallyBlock;

		/**
		 * Instructions covered by finally handler.
		 */
		final Set<AbstractInsnNode> inside = new HashSet<AbstractInsnNode>();

		private Matcher(final IFilterOutput output, final MethodNode m,
				final TryCatchBlockNode finallyBlock) {
			this.output = output;
			this.m = m;
			this.finallyBlock = finallyBlock;
		}

		private void filter() {
			final AbstractInsnNode e = next(finallyBlock.handler);
			final int size = size(e);
			if (size <= 0) {
				return;
			}

			for (final TryCatchBlockNode t : m.tryCatchBlocks) {
				if (t.handler == finallyBlock.handler) {
					AbstractInsnNode i = t.start;
					while (i != t.end) {
						inside.add(i);
						i = i.getNext();
					}
				}
			}

			for (final TryCatchBlockNode t : m.tryCatchBlocks) {
				if (t.handler == finallyBlock.handler) {
					boolean continues = false;
					AbstractInsnNode i = t.start;

					while (i != t.end) {
						switch (i.getType()) {
						case AbstractInsnNode.FRAME:
						case AbstractInsnNode.LINE:
						case AbstractInsnNode.LABEL:
							break;
						case AbstractInsnNode.JUMP_INSN:
							final AbstractInsnNode jumpTarget = next(
									((JumpInsnNode) i).label);
							if (!inside.contains(jumpTarget)) {
								merge(size, e, jumpTarget);
							}
							continues = i.getOpcode() != Opcodes.GOTO;
							break;
						default:
							continues = true;
							break;
						}
						i = i.getNext();
					}

					i = next(i);
					if (continues && !inside.contains(i)) {
						merge(size, e, i);
					}
				}

				if (t != finallyBlock && t.start == finallyBlock.start
						&& t.end == finallyBlock.end) {
					// javac's empty catch, merge after ASTORE
					final AbstractInsnNode i = next(next(t.handler));
					if (!inside.contains(i)) {
						merge(size, e, i);
					}
				}
			}
		}

		private void merge(final int size, AbstractInsnNode e,
				AbstractInsnNode n) {
			if (!isSame(size, e, n)) {
				return;
			}
			output.ignore(e, e);
			e = next(e);
			for (int i = 0; i < size; i++) {
				output.merge(e, n);
				e = next(e);
				n = next(n);
			}
			output.ignore(e, next(e));
			if (n != null && n.getOpcode() == Opcodes.GOTO) {
				output.ignore(n, n);
			}
		}

	}

	private static boolean isSame(final int size, AbstractInsnNode e,
			AbstractInsnNode n) {
		e = next(e);
		for (int i = 0; i < size; i++) {
			if (n == null || e.getOpcode() != n.getOpcode()) {
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
			// when always completes abruptly
			return 0;
		}
		final int var = ((VarInsnNode) i).var;
		int size = -1;
		do {
			size++;
			i = next(i);
			if (i == null) {
				// when always completes abruptly
				return 0;
			}
		} while (!(Opcodes.ALOAD == i.getOpcode()
				&& var == ((VarInsnNode) i).var));
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
