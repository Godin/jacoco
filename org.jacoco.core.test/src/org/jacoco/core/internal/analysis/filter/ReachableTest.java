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

import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

public class ReachableTest {

	@Test
	public void test() {
		final MethodNode m = gen();

		final Set<AbstractInsnNode> reachable = Reachable.compute(m);

		for (int i = 0; i < m.instructions.size(); i++) {
			final AbstractInsnNode instruction = m.instructions.get(i);
			if (instruction.getType() != AbstractInsnNode.LABEL
					&& !reachable.contains(instruction)) {
				System.out.println("Unreachable " + i);
			}
		}
	}

	private MethodNode gen() {
		MethodNode m = new MethodNode(0, "name", "()V", null, null);
		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();

		m.visitTryCatchBlock(start, end, handler, null);

		m.visitLabel(start);
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(end);

		m.visitInsn(Opcodes.NOP);

		m.visitLabel(handler);
		m.visitInsn(Opcodes.RETURN);

		return m;
	}

}
