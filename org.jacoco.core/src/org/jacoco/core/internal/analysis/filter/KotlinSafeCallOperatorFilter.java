/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.analysis.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters bytecode that Kotlin compiler generates for chains of safe call
 * operators ({@code ?.}).
 */
final class KotlinSafeCallOperatorFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		for (final ArrayList<JumpInsnNode> chain : findChains(methodNode)) {
			// if (chain.size() == 1) {
			// continue;
			// }
			final JumpInsnNode lastIfNullInstruction = chain
					.get(chain.size() - 1);
			final AbstractInsnNode nullTarget = AbstractMatcher
					.skipNonOpcodes(lastIfNullInstruction.label);
			final ArrayList<IFilterOutput.InstructionBranch> nullBranch = new ArrayList<IFilterOutput.InstructionBranch>();
			for (final AbstractInsnNode ifNullInstruction : chain) {
				nullBranch.add(new IFilterOutput.InstructionBranch(
						ifNullInstruction, 1));
			}
			final AbstractInsnNode ifNonNullInstruction = nullTarget
					// TODO followed by elvis?
					// .getPrevious().getPrevious();
					.getPrevious().getPrevious().getPrevious();
			if (ifNonNullInstruction.getOpcode() == Opcodes.IFNONNULL) {
				nullBranch.add(new IFilterOutput.InstructionBranch(
						ifNonNullInstruction, 0));
				output.replaceBranches(ifNonNullInstruction,
						Arrays.<Collection<IFilterOutput.InstructionBranch>> asList( //
								// null branch
								nullBranch,
								// non null branch
								Collections.singletonList(
										new IFilterOutput.InstructionBranch(
												ifNonNullInstruction, 1))));
			}
			for (final AbstractInsnNode ifNullInstruction : chain) {
				output.replaceBranches(ifNullInstruction,
						Arrays.<Collection<IFilterOutput.InstructionBranch>> asList(
								// non null branch
								Collections.singletonList(
										new IFilterOutput.InstructionBranch(
												ifNullInstruction, 0)),
								// null branch
								nullBranch));
			}
		}
	}

	/**
	 * "optimized" chain:
	 *
	 * <pre>
	 * DUP
	 * IFNULL label
	 * ... // call 0
	 *
	 * ...
	 *
	 * DUP
	 * IFNULL label
	 * ... // call N
	 *
	 * label:
	 * POP
	 * </pre>
	 *
	 * "unoptimized" chain:
	 *
	 * <pre>
	 * ALOAD v0
	 * IFNULL label
	 * ... // call 0
	 *
	 * ...
	 *
	 * ASTORE v1
	 * ALOAD v1
	 * IFNULL label
	 * ... // call N
	 *
	 * label:
	 * ACONST_NULL
	 * </pre>
	 */
	private static Collection<ArrayList<JumpInsnNode>> findChains(
			final MethodNode methodNode) {
		final HashMap<AbstractInsnNode, ArrayList<JumpInsnNode>> chains = new HashMap<AbstractInsnNode, ArrayList<JumpInsnNode>>();
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getOpcode() != Opcodes.IFNULL) {
				continue;
			}
			final JumpInsnNode jump = (JumpInsnNode) i;
			final LabelNode label = jump.label;
			final AbstractInsnNode target = AbstractMatcher
					.skipNonOpcodes(label);
			ArrayList<JumpInsnNode> chain = chains.get(label);
			if (target.getOpcode() == Opcodes.POP) {
				if (i.getPrevious().getOpcode() != Opcodes.DUP) {
					continue;
				}
			} else if (target.getOpcode() == Opcodes.ACONST_NULL) {
				if (i.getPrevious().getOpcode() != Opcodes.ALOAD) {
					continue;
				}
				if (chain != null) {
					final AbstractInsnNode p1 = i.getPrevious();
					final AbstractInsnNode p2 = p1.getPrevious();
					if (p2 == null || p2.getOpcode() != Opcodes.ASTORE
							|| ((VarInsnNode) p1).var != ((VarInsnNode) p2).var) {
						continue;
					}
				}
			} else {
				continue;
			}
			if (chain == null) {
				chain = new ArrayList<JumpInsnNode>();
				chains.put(label, chain);
			}
			chain.add(jump);
		}
		return chains.values();
	}

}
