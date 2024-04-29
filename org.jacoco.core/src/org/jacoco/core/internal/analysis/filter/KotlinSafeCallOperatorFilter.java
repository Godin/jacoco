/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * TODO see
 * https://github.com/JetBrains/kotlin/commit/9325660f06ebb7d8ef201cf9e4dea49a71d8b77c
 * https://github.com/JetBrains/kotlin/blob/f835be79fa3013f7fdaf7fea0a28a476422afd05/compiler/backend/src/org/jetbrains/kotlin/codegen/optimization/temporaryVals/TemporaryVariablesEliminationTransformer.kt#L333
 */
final class KotlinSafeCallOperatorFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}
		final HashMap<AbstractInsnNode, ArrayList<AbstractInsnNode>> map = new HashMap<AbstractInsnNode, ArrayList<AbstractInsnNode>>();
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getOpcode() == Opcodes.IFNULL
					&& i.getPrevious().getOpcode() == Opcodes.DUP) {
				final LabelNode label = ((JumpInsnNode) i).label;
				ArrayList<AbstractInsnNode> list = map.get(label);
				if (list == null) {
					list = new ArrayList<AbstractInsnNode>();
					map.put(label, list);
				}
				list.add(i);
			}
		}
		for (final ArrayList<AbstractInsnNode> list : map.values()) {
			if (list.size() > 1) {
				final AbstractInsnNode m = list.get(0);
				for (int i = 1; i < list.size(); i++) {
					output.merge(m, list.get(i));
				}
			}
		}
	}

}
