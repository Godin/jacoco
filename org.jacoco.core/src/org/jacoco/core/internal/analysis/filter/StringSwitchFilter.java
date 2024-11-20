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
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters code that is generated by ECJ for a <code>switch</code> statement
 * with a <code>String</code> and by Kotlin compiler 1.5 and above for a
 * <code>when</code> expression with a <code>String</code>.
 */
public final class StringSwitchFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher(methodNode);
		for (final AbstractInsnNode i : methodNode.instructions) {
			matcher.match(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		private final MethodNode methodNode;

		Matcher(MethodNode methodNode) {
			this.methodNode = methodNode;
		}

		public void match(final AbstractInsnNode start,
				final IFilterOutput output) {

			if (start.getOpcode() != /* ECJ */ Opcodes.ASTORE
					&& start.getOpcode() != /* Kotlin */ Opcodes.ALOAD) {
				return;
			}
			cursor = start;
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode",
					"()I");
			nextIsSwitch();
			if (cursor == null) {
				return;
			}
			vars.put("s", (VarInsnNode) start);

			final AbstractInsnNode s = cursor;
			final int hashCodes;
			final LabelNode defaultLabel;
			if (s.getOpcode() == Opcodes.LOOKUPSWITCH) {
				final LookupSwitchInsnNode lookupSwitch = (LookupSwitchInsnNode) cursor;
				defaultLabel = lookupSwitch.dflt;
				hashCodes = lookupSwitch.labels.size();
			} else {
				final TableSwitchInsnNode tableSwitch = (TableSwitchInsnNode) cursor;
				defaultLabel = tableSwitch.dflt;
				hashCodes = tableSwitch.labels.size();
			}

			if (hashCodes == 0) {
				return;
			}

			final Replacements replacements = new Replacements(
					new InstructionComparator(methodNode,
							skipNonOpcodes(defaultLabel)));
			replacements.add(skipNonOpcodes(defaultLabel), s, 0);

			for (int i = 0; i < hashCodes; i++) {
				while (true) {
					nextIsVar(Opcodes.ALOAD, "s");
					nextIs(Opcodes.LDC);
					nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/String",
							"equals", "(Ljava/lang/Object;)Z");
					// jump to case
					nextIs(Opcodes.IFNE);
					if (cursor == null) {
						return;
					}
					replacements.add(
							skipNonOpcodes(((JumpInsnNode) cursor).label),
							cursor, 1);

					if (cursor.getNext().getOpcode() == Opcodes.GOTO) {
						// end of comparisons for same hashCode
						// jump to default
						nextIs(Opcodes.GOTO);
						replacements.add(
								skipNonOpcodes(((JumpInsnNode) cursor).label),
								cursor, 0);
						break;
					} else if (cursor.getNext() == defaultLabel) {
						replacements.add(skipNonOpcodes(defaultLabel), cursor,
								0);
						break;
					}
				}
			}

			output.ignore(s.getNext(), cursor);
			output.replaceBranches(s, replacements);
		}
	}

	static class Replacements extends
			TreeMap<AbstractInsnNode, List<IFilterOutput.InstructionBranch>> {
		Replacements(final Comparator<AbstractInsnNode> comparator) {
			super(comparator);
		}

		void add(final AbstractInsnNode target,
				final AbstractInsnNode fromInstruction, final int fromBranch) {
			List<IFilterOutput.InstructionBranch> list = get(target);
			if (list == null) {
				list = new ArrayList<IFilterOutput.InstructionBranch>();
				put(target, list);
			}
			list.add(new IFilterOutput.InstructionBranch(fromInstruction,
					fromBranch));
		}
	}

	private static class InstructionComparator
			implements Comparator<AbstractInsnNode> {
		private final AbstractInsnNode defaultCase;
		private final MethodNode methodNode;

		InstructionComparator(final MethodNode methodNode,
				final AbstractInsnNode defaultCase) {
			this.methodNode = methodNode;
			this.defaultCase = defaultCase;
		}

		public int compare(final AbstractInsnNode i1,
				final AbstractInsnNode i2) {
			if (i1 == i2)
				return 0;
			if (i1 == defaultCase)
				return -1;
			if (i2 == defaultCase)
				return 1;
			return methodNode.instructions.indexOf(i1)
					- methodNode.instructions.indexOf(i2);
		}
	}

}
