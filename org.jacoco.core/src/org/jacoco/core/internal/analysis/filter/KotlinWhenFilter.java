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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Filters bytecode that Kotlin compiler generates for <code>when</code>
 * expressions and statements with subject of type <code>enum class</code> or
 * <code>sealed class</code>.
 */
public final class KotlinWhenFilter implements IFilter {

	private static final String EXCEPTION = "kotlin/NoWhenBranchMatchedException";

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher(methodNode);
		for (final AbstractInsnNode i : methodNode.instructions) {
			matcher.match(i, output);
			matcher.matchNullableEnum(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		private final MethodNode methodNode;

		public Matcher(final MethodNode methodNode) {
			this.methodNode = methodNode;
		}

		void match(final AbstractInsnNode start, final IFilterOutput output) {
			if (start.getType() != AbstractInsnNode.LABEL) {
				return;
			}
			cursor = start;

			nextIsType(Opcodes.NEW, EXCEPTION);
			nextIs(Opcodes.DUP);
			nextIsInvoke(Opcodes.INVOKESPECIAL, EXCEPTION, "<init>", "()V");
			nextIs(Opcodes.ATHROW);

			for (AbstractInsnNode i = cursor; i != null; i = i.getPrevious()) {
				if (i.getOpcode() == Opcodes.IFEQ
						&& ((JumpInsnNode) i).label == start) {
					output.ignore(i, i);
					output.ignore(start, cursor);
					return;

				} else if (getDefaultLabel(i) == start) {
					ignoreDefaultBranch(methodNode, i, output);
					output.ignore(start, cursor);
					return;

				}
			}
		}

		void matchNullableEnum(final AbstractInsnNode start,
				final IFilterOutput output) {
			if (start.getOpcode() != Opcodes.DUP) {
				return;
			}
			cursor = start;
			// https://github.com/JetBrains/kotlin/blob/v2.0.0/compiler/backend/src/org/jetbrains/kotlin/codegen/when/EnumSwitchCodegen.java#L46
			nextIs(Opcodes.IFNONNULL);
			final JumpInsnNode jump1 = (JumpInsnNode) cursor;
			nextIs(Opcodes.POP);
			nextIs(Opcodes.ICONST_M1);
			nextIs(Opcodes.GOTO);
			final JumpInsnNode jump2 = (JumpInsnNode) cursor;
			nextIs(Opcodes.GETSTATIC);
			final FieldInsnNode fieldInsnNode = (FieldInsnNode) cursor;
			// https://github.com/JetBrains/kotlin/blob/v2.0.0/compiler/backend/src/org/jetbrains/kotlin/codegen/when/WhenByEnumsMapping.java#L27-L28
			if (fieldInsnNode == null
					|| !fieldInsnNode.owner.endsWith("$WhenMappings")
					|| !fieldInsnNode.name.startsWith("$EnumSwitchMapping$")) {
				return;
			}
			nextIs(Opcodes.SWAP);
			nextIs(Opcodes.INVOKEVIRTUAL); // ordinal()I
			nextIs(Opcodes.IALOAD);
			nextIsSwitch();
			if (cursor != null
					&& skipNonOpcodes(jump1.label) == skipNonOpcodes(
							jump2.getNext())
					&& skipNonOpcodes(jump2.label) == cursor) {
				output.ignore(start, cursor.getPrevious());
			}
		}
	}

	private static LabelNode getDefaultLabel(final AbstractInsnNode i) {
		switch (i.getOpcode()) {
		case Opcodes.LOOKUPSWITCH:
			return ((LookupSwitchInsnNode) i).dflt;
		case Opcodes.TABLESWITCH:
			return ((TableSwitchInsnNode) i).dflt;
		default:
			return null;
		}
	}

	private static void ignoreDefaultBranch(final MethodNode methodNode,
			final AbstractInsnNode switchNode, final IFilterOutput output) {
		final List<LabelNode> labels;
		if (switchNode.getOpcode() == Opcodes.LOOKUPSWITCH) {
			labels = ((LookupSwitchInsnNode) switchNode).labels;
		} else {
			labels = ((TableSwitchInsnNode) switchNode).labels;
		}
		final LabelNode defaultLabel = getDefaultLabel(switchNode);

		final StringSwitchFilter.Replacements replacements = new StringSwitchFilter.Replacements(
				new StringSwitchFilter.InstructionComparator(methodNode, null));
		int branch = 1;
		for (final LabelNode label : labels) {
			final AbstractInsnNode target = AbstractMatcher
					.skipNonOpcodes(label);
			if (label != defaultLabel && !replacements.containsKey(target)) {
				replacements.add(target, switchNode, branch);
				branch++;
			}
		}
		output.replaceBranches(switchNode, replacements);
	}

}
