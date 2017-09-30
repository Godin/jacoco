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
 * Filters duplicates of finally blocks that compiler generates.
 */
public final class FinallyFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
			if (tryCatchBlock.type == null) {
				final AbstractInsnNode e = next(tryCatchBlock.handler);
				final int size = size(e);
				if (size > 0) {
					filter(output, size, e);
				}
			}
		}
	}

	/**
	 * <pre>
	 *     ASTORE ex
	 *     ...
	 *     ALOAD ex
	 *     ATHROW
	 * </pre>
	 *
	 * @return number of instructions inside given finally handler
	 */
	private static int size(AbstractInsnNode e) {
		if (e.getOpcode() != Opcodes.ASTORE) {
			return 0;
		}
		final int var = ((VarInsnNode) e).var;
		int size = 0;
		while (true) {
			e = next(e);
			if (e == null) {
				return 0;
			}
			if (Opcodes.ALOAD == e.getOpcode()
					&& ((VarInsnNode) e).var == var) {
				break;
			}
			size++;
		}
		e = next(e);
		if (Opcodes.ATHROW != e.getOpcode()) {
			return 0;
		}
		return size;
	}

	private static void filter(final IFilterOutput output, final int size,
			AbstractInsnNode e) {
		// seems that ASTORE has line number
		// of a first instruction of a finally block in case of javac,
		// and of an opening curly brace of a finally block in case of ecj
		output.ignore(e, e);
		e = next(e);
		for (int i = 0; i < size; i++) {
			e = next(e);
		}
		// seems that ALOAD and ATHROW have line number
		// of a last instruction of a finally block in case of javac,
		// and of a closing curly brace of a finally block in case of ecj
		output.ignore(e, next(e));
	}

	private static AbstractInsnNode next(AbstractInsnNode node) {
		do {
			node = node.getNext();
		} while (node != null && (node.getType() == AbstractInsnNode.FRAME
				|| node.getType() == AbstractInsnNode.LABEL
				|| node.getType() == AbstractInsnNode.LINE));
		return node;
	}

}
