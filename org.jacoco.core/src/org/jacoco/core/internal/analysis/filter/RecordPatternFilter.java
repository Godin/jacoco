/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters code that is generated for record patterns.
 */
final class RecordPatternFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Matcher matcher = new Matcher();
		for (final TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if ("java/lang/Throwable".equals(t.type)) {
				matcher.match(t.handler, output);
			}
		}
		for (final AbstractInsnNode i : methodNode.instructions) {
			if (i.getOpcode() == Opcodes.INVOKEDYNAMIC) {
				matcher.matchSwitch(i.getPrevious(), output);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {
		private final HashSet<AbstractInsnNode> nested = new HashSet<AbstractInsnNode>();

		void match(final AbstractInsnNode start, final IFilterOutput output) {
			cursor = start;
			nextIsVar(Opcodes.ASTORE, "cause");
			nextIsType(Opcodes.NEW, "java/lang/MatchException");
			nextIs(Opcodes.DUP);
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"toString", "()Ljava/lang/String;");
			nextIsVar(Opcodes.ALOAD, "cause");
			nextIsInvoke(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
					"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor != null) {
				output.ignore(start, cursor);
			}
		}

		/**
		 * TODO example of a few cases in outer what results in LOOKUPSWITCH
		 *
		 * <pre>
		 * switch (o) {
		 *     case Container(...) -> ...
		 *     case Container(...) -> ...
		 *     case String s -> ...
		 *     default -> ...
		 * }
		 * </pre>
		 *
		 * TODO name "matchOuterSwitch"?
		 */
		private void matchSwitch(final AbstractInsnNode start,
				final IFilterOutput output) {
			cursor = start;
			nextIsInvokeDynamic("typeSwitch",
					"java/lang/runtime/SwitchBootstraps", "typeSwitch");
			nextIsSwitch();
			if (cursor == null) {
				return;
			}
			final AbstractInsnNode switchNode = cursor;
			if (nested.contains(switchNode)) {
				// TODO unpredictable when replacement depends on another?
				// TODO reproducible with JDK 25
				// when branches of nested replaced first
				// and then used to compute branches of outer
				// change in MethodCoverageCalculator fixes this
				// return;
			}
			final Replacements replacements = new Replacements();
			walk(switchNode, replacements, output);
			final AbstractInsnNode defaultLabel = getDefaultLabel(switchNode);
			if (!matchExhaustive(defaultLabel, output)) {
				replacements.add(defaultLabel, switchNode, 0);
			}
			output.replaceBranches(switchNode, replacements);
		}

		/**
		 * TODO name "matchSwitch"?
		 */
		private void walk(final AbstractInsnNode switchNode,
				final Replacements replacements, final IFilterOutput output) {
			final List<LabelNode> labels = getLabels(switchNode);
			final LabelNode defaultLabel = getDefaultLabel(switchNode);
			int branchIndex = 0;
			for (final LabelNode label : labels) {
				if (label == defaultLabel) {
					continue;
				}
				branchIndex++;
				if (!matchNested(switchNode, label, replacements, output)) {
					replacements.add(label, switchNode, branchIndex);
				}
			}
		}

		/**
		 * TODO name "matchInnerSwitch"?
		 */
		private boolean matchNested(final AbstractInsnNode switchNode,
				final LabelNode label, final Replacements replacements,
				final IFilterOutput output) {
			cursor = label;
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.CHECKCAST);
			if (cursor == null) {
				cursor = label;
				nextIs(Opcodes.ALOAD);
			}
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.INVOKEVIRTUAL);
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.ICONST_0);
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.ILOAD);
			nextIsInvokeDynamic("typeSwitch",
					"java/lang/runtime/SwitchBootstraps", "typeSwitch");
			nextIsSwitch();
			if (cursor == null) {
				return false;
			}
			final AbstractInsnNode nestedSwitchNode = cursor;
			cursor = getDefaultLabel(nestedSwitchNode);
			next(/* ICONST_x, BIPIUSH, SIPUSH */); // restartIndex
			nextIs(Opcodes.ISTORE);
			final VarInsnNode store = (VarInsnNode) cursor;
			nextIs(Opcodes.GOTO);
			final JumpInsnNode jumpToOuter = (JumpInsnNode) cursor;
			if (cursor == null) {
				return false;
			}
			cursor = jumpToOuter.label;
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.ILOAD);
			final VarInsnNode load = (VarInsnNode) cursor;
			nextIsInvokeDynamic("typeSwitch",
					"java/lang/runtime/SwitchBootstraps", "typeSwitch");
			nextIsSwitch();
			if (cursor != switchNode || store.var != load.var) {
				return false;
			}
			walk(nestedSwitchNode, replacements, output);
			nested.add(nestedSwitchNode);
			output.ignore(nestedSwitchNode, nestedSwitchNode);
			output.ignore(getDefaultLabel(nestedSwitchNode), jumpToOuter);
			return true;
		}

		/**
		 * @see ExhaustiveSwitchFilter
		 */
		private boolean matchExhaustive(final AbstractInsnNode defaultLabel,
				final IFilterOutput output) {
			cursor = defaultLabel;
			nextIsType(Opcodes.NEW, "java/lang/MatchException");
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.ACONST_NULL);
			nextIs(Opcodes.ACONST_NULL);
			nextIsInvoke(Opcodes.INVOKESPECIAL, "java/lang/MatchException",
					"<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return false;
			}
			output.ignore(defaultLabel, cursor);
			return true;
		}
	}

	private static List<LabelNode> getLabels(
			final AbstractInsnNode switchNode) {
		switch (switchNode.getOpcode()) {
		case Opcodes.TABLESWITCH:
			return ((TableSwitchInsnNode) switchNode).labels;
		case Opcodes.LOOKUPSWITCH:
			return ((LookupSwitchInsnNode) switchNode).labels;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static LabelNode getDefaultLabel(
			final AbstractInsnNode switchNode) {
		switch (switchNode.getOpcode()) {
		case Opcodes.TABLESWITCH:
			return ((TableSwitchInsnNode) switchNode).dflt;
		case Opcodes.LOOKUPSWITCH:
			return ((LookupSwitchInsnNode) switchNode).dflt;
		default:
			throw new IllegalArgumentException();
		}
	}

}
