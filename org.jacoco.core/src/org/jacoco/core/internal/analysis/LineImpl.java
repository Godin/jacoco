/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.BitSet;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

/**
 * Implementation of {@link ILine}.
 */
public abstract class LineImpl implements ILine {

	/** Max instruction counter value for which singletons are created */
	private static final int SINGLETON_INS_LIMIT = 8;

	/** Max branch counter value for which singletons are created */
	private static final int SINGLETON_BRA_LIMIT = 4;

	/**
	 * Frequency of branches on a line
	 * <pre>
	 * java -jar org.jacoco.cli/target/org.jacoco.cli-0.8.14-SNAPSHOT-nodeps.jar \
	 *   classinfo \
	 *   ~/.java-select/versions/8/jre/lib/rt.jar \
	 *   --verbose \
	 *   | grep -v "method" \
	 *   | grep -v "class" \
	 *   | tr -s ' ' \
	 *   | cut -d' ' -f3 \
	 *   | sort \
	 *   | uniq -c \
	 *   | sort -r | head -n10
	 * </pre>
	 *
	 * TODO in-memory size?
	 */
	// CSV
	// | sed 's/^ *//g' | sed 's/ /\, /'
	private static final LineImpl[][][][] SINGLETONS;

	static {
		SINGLETONS = new LineImpl[SINGLETON_INS_LIMIT + 1][][][];
		for (int i = 0; i <= SINGLETON_INS_LIMIT; i++) {
			SINGLETONS[i] = new LineImpl[SINGLETON_INS_LIMIT + 1][][];
			for (int j = 0; j <= SINGLETON_INS_LIMIT; j++) {
				SINGLETONS[i][j] = new LineImpl[SINGLETON_BRA_LIMIT * 2 + 1][];
				for (int branches = 0; branches <= SINGLETON_BRA_LIMIT
						+ SINGLETON_BRA_LIMIT; branches++) {
					SINGLETONS[i][j][branches] = new LineImpl[1 << branches];
					for (int coveredBranches = 0; coveredBranches < 1 << branches; coveredBranches++) {
						int bm = 0;
						int bc = 0;
						for (int branch = 0; branch < branches; branch++) {
							if (get(coveredBranches, branch)) {
								bc++;
							} else {
								bm++;
							}
						}
						SINGLETONS[i][j][branches][coveredBranches] = new Fix(i,
								j, bm, bc, coveredBranches);
					}
				}
			}
		}
	}

	/**
	 * Empty line without instructions or branches.
	 */
	public static final LineImpl EMPTY = SINGLETONS[0][0][0][0];

	public static LineImpl getInstance(final CounterImpl instructions,
			final CounterImpl branches, final int coveredBranches) {
		if (instructions.getMissedCount() > SINGLETON_INS_LIMIT
				|| instructions.getCoveredCount() > SINGLETON_INS_LIMIT
				|| branches.getMissedCount() > SINGLETON_BRA_LIMIT
				|| branches.getCoveredCount() > SINGLETON_BRA_LIMIT) {
			final Var result = new Var(instructions, branches);
			result.coveredBranches = coveredBranches;
			return result;
		}
		return SINGLETONS[instructions.getMissedCount()][instructions
				.getCoveredCount()][branches.getTotalCount()][coveredBranches];
	}

	public static LineImpl getInstance(final CounterImpl instructions,
			final CounterImpl branches) {
		final int coveredBranches = ((1 << branches.getTotalCount())
				- 1) >> branches.getMissedCount();
		return getInstance(instructions, branches, coveredBranches);
	}

	/**
	 * Mutable version.
	 */
	private static final class Var extends LineImpl {
		Var(final CounterImpl instructions, final CounterImpl branches) {
			super(instructions, branches);
		}

		@Override
		public LineImpl increment(final ICounter instructions,
				final ICounter branches, final BitSet coveredBranches) {
			this.instructions = this.instructions.increment(instructions);
			this.branches = this.branches.increment(branches);
			if (coveredBranches == null) {
				// is not line of MethodCoverageImpl
				return this;
			}
			this.coveredBranches = append(branches.getTotalCount(), coveredBranches);
			return this;
		}
	}

	/**
	 * Immutable version.
	 */
	private static final class Fix extends LineImpl {
		public Fix(final int im, final int ic, final int bm, final int bc,
				final int coveredBranches) {
			super(CounterImpl.getInstance(im, ic),
					CounterImpl.getInstance(bm, bc));
			this.coveredBranches = coveredBranches;
		}

		@Override
		public LineImpl increment(ICounter instructions, ICounter branches,
				BitSet coveredBranches) {
			final CounterImpl incrementedInstructions = this.instructions
					.increment(instructions);
			final CounterImpl incrementedBranches = this.branches
					.increment(branches);
			if (coveredBranches == null) {
				// is not line of MethodCoverageImpl
				return getInstance(incrementedInstructions,
						incrementedBranches);
			}
			final int bitSet = this.append(branches.getTotalCount(), coveredBranches);
			return getInstance(incrementedInstructions, incrementedBranches,
					bitSet);
		}
	}

	/** instruction counter */
	protected CounterImpl instructions;

	/** branch counter */
	protected CounterImpl branches;

	/** bitmask of covered branches */
	protected int coveredBranches;

	private LineImpl(final CounterImpl instructions,
			final CounterImpl branches) {
		this.instructions = instructions;
		this.branches = branches;
	}

	/**
	 * @deprecated used only in tests, use
	 *             {@link #increment(ICounter, ICounter, BitSet)} instead
	 */
	@Deprecated
	public final LineImpl increment(final ICounter instructions,
			final ICounter branches) {
		return increment(instructions, branches, null);
	}

	/**
	 * Adds the given counters to this line.
	 *
	 * @param instructions
	 *            instructions to add
	 * @param branches
	 *            branches to add
	 * @param coveredBranches
	 *            covered branches to add or {@code null}
	 * @return instance with new counter values
	 */
	public abstract LineImpl increment(final ICounter instructions,
			final ICounter branches, final BitSet coveredBranches);

	// === ILine implementation ===

	public int getStatus() {
		return instructions.getStatus() | branches.getStatus();
	}

	public ICounter getInstructionCounter() {
		return instructions;
	}

	public ICounter getBranchCounter() {
		return branches;
	}

	/**
	 * @return covered branches in the order of bytecode traversal
	 */
	public final BitSet getCoveredBranches() {
		final int size = Math.min(branches.getTotalCount(), 31);
		final BitSet result = new BitSet(size);
		for (int i = 0; i < size; i++) {
			result.set(i, get(coveredBranches, i));
		}
		return result;
	}

	protected int append(final int size, final BitSet coveredBranches) {
		final int thisTotalCount = this.branches.getTotalCount();
		int bitSet = this.coveredBranches;
		for (int i = 0; i < size; i++) {
			if (coveredBranches.get(i)) {
				bitSet = set(bitSet, thisTotalCount + i);
			}
		}
		return bitSet;
	}

	private static int set(final int bitSet, final int index) {
		if (index > 31) {
			return bitSet;
		}
		return bitSet | (1 << index);
	}

	private static boolean get(final int bitSet, final int index) {
		if (index > 31) {
			return false;
		}
		return (bitSet & (1 << index)) != 0;
	}

	@Override
	public int hashCode() {
		return 23 * instructions.hashCode() ^ branches.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ILine) {
			final ILine that = (ILine) obj;
			return this.instructions.equals(that.getInstructionCounter())
					&& this.branches.equals(that.getBranchCounter());
		}
		return false;
	}

}
