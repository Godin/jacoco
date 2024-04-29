/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Fabian Mastenbroek - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * TODO https://github.com/jacoco/jacoco/issues/1351
 */
public final class KotlinWipFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!KotlinGeneratedFilter.isKotlinClass(context)) {
			return;
		}
		final HashMap<LabelNode, List<AbstractInsnNode>> map = new HashMap<LabelNode, List<AbstractInsnNode>>();
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getOpcode() == Opcodes.IFNULL
					&& i.getPrevious().getOpcode() == Opcodes.DUP) {
				final LabelNode label = ((JumpInsnNode) i).label;
				List<AbstractInsnNode> list = map.get(label);
				if (list == null) {
					list = new ArrayList<AbstractInsnNode>();
					map.put(label, list);
				}
				list.add(i);
			}
		}
		for (List<AbstractInsnNode> list : map.values()) {
			if (list.size() > 1) {
				final AbstractInsnNode m = list.get(0);
				for (int i = 1; i < list.size(); i++) {
					output.merge(m, list.get(i));
				}
			}
		}
	}

}
