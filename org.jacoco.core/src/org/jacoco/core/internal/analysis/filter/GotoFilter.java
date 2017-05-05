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

public final class GotoFilter implements IFilter {

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		boolean afterJump = false;
		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = next(i)) {
			if (i.getOpcode() == Opcodes.GOTO && !afterJump) {
				output.ignore(i, i);
			}
			afterJump = i.getType() == AbstractInsnNode.JUMP_INSN;
		}
	}

	private static AbstractInsnNode next(AbstractInsnNode i) {
		do {
			i = i.getNext();
		} while (i != null && (i.getType() == AbstractInsnNode.FRAME
				|| i.getType() == AbstractInsnNode.LABEL
				|| i.getType() == AbstractInsnNode.LINE));
		return i;
	}

}
