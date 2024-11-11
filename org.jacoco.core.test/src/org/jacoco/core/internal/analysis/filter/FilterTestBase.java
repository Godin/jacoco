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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * Base class for tests of {@link IFilter} implementations.
 */
public abstract class FilterTestBase {

	protected final FilterContextMock context = new FilterContextMock();

	private final List<Range> ignoredRanges = new ArrayList<Range>();

	private final Map<AbstractInsnNode, List<IFilterOutput.BranchReplacement>> replacements = new HashMap<AbstractInsnNode, List<IFilterOutput.BranchReplacement>>();

	protected final IFilterOutput output = new IFilterOutput() {
		public void ignore(final AbstractInsnNode fromInclusive,
				final AbstractInsnNode toInclusive) {
			final Range range = new Range();
			range.fromInclusive = fromInclusive;
			range.toInclusive = toInclusive;
			ignoredRanges.add(range);
		}

		public void merge(final AbstractInsnNode i1,
				final AbstractInsnNode i2) {
			fail();
		}

		public void replaceBranches(final AbstractInsnNode instruction,
				final List<BranchReplacement> replacements) {
			FilterTestBase.this.replacements.put(instruction, replacements);
		}
	};

	final void assertIgnored(Range... ranges) {
		assertArrayEquals(ranges, ignoredRanges.toArray(new Range[0]));
	}

	final void assertMethodIgnored(final MethodNode m) {
		assertIgnored(
				new Range(m.instructions.getFirst(), m.instructions.getLast()));
	}

	final void assertNoReplacedBranches() {
		assertTrue(replacements.isEmpty());
	}

	final void assertReplacedBranches(final AbstractInsnNode source,
			final List<IFilterOutput.BranchReplacement> expected) {
		final List<IFilterOutput.BranchReplacement> actual = this.replacements
				.get(source);
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).branch, actual.get(i).branch);
			assertEquals(expected.get(i).instruction,
					actual.get(i).instruction);
			assertEquals(expected.get(i).branchIndex,
					actual.get(i).branchIndex);
		}
	}

	final void assertDefaultBranchIgnored(AbstractInsnNode switchNode) {
		final int labels;
		if (switchNode.getOpcode() == Opcodes.LOOKUPSWITCH) {
			labels = ((LookupSwitchInsnNode) switchNode).labels.size();
		} else {
			labels = ((TableSwitchInsnNode) switchNode).labels.size();
		}
		final ArrayList<IFilterOutput.BranchReplacement> replacements = new ArrayList<IFilterOutput.BranchReplacement>();
		for (int i = 1; i <= labels; i++) {
			replacements.add(
					new IFilterOutput.BranchReplacement(i - 1, switchNode, i));
		}
		assertReplacedBranches(switchNode, replacements);
	}

	static class Range {
		AbstractInsnNode fromInclusive;
		AbstractInsnNode toInclusive;

		Range() {
		}

		Range(AbstractInsnNode fromInclusive, AbstractInsnNode toInclusive) {
			this.fromInclusive = fromInclusive;
			this.toInclusive = toInclusive;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() == Range.class) {
				final Range other = (Range) obj;
				return this.fromInclusive.equals(other.fromInclusive)
						&& this.toInclusive.equals(other.toInclusive);
			}
			return false;
		}
	}

}
