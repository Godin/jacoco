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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @deprecated
 */
@Deprecated
public class FinallyPreviousFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		for (final TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
			if (tryCatchBlock.type == null) {
				filter(tryCatchBlock, output);
			}
		}
	}

	private void filter(final TryCatchBlockNode tryCatchBlock,
			final IFilterOutput output) {
		final AbstractInsnNode n = next(tryCatchBlock.end);
		final AbstractInsnNode e = next(tryCatchBlock.handler);
		final int size = size(e);
		if (size == 0) {
			return;
		}
		filter(output, size, e, n);
	}

	private static void filter(final IFilterOutput output, final int size,
			AbstractInsnNode e, AbstractInsnNode n) {
		output.ignore(e, e);
		e = next(e);
		for (int i = 0; i < size; i++) {
			if (e.getOpcode() != n.getOpcode()) {
				throw new AssertionError();
			}
			output.merge(e, n);
			n = next(n);
			e = next(e);
		}
		output.ignore(e, next(e));
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
			if (Opcodes.MONITOREXIT == i.getOpcode()) {
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
