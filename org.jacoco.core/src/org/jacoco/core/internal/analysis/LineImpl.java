/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
	private static final int SINGLETON_BRA_LIMIT = 0;

	private static final LineImpl[][][][] SINGLETONS = new LineImpl[SINGLETON_INS_LIMIT
			+ 1][][][];

	static {
		for (int i = 0; i <= SINGLETON_INS_LIMIT; i++) {
			SINGLETONS[i] = new LineImpl[SINGLETON_INS_LIMIT + 1][][];
			for (int j = 0; j <= SINGLETON_INS_LIMIT; j++) {
				SINGLETONS[i][j] = new LineImpl[SINGLETON_BRA_LIMIT + 1][];
				for (int k = 0; k <= SINGLETON_BRA_LIMIT; k++) {
					SINGLETONS[i][j][k] = new LineImpl[SINGLETON_BRA_LIMIT + 1];
					for (int l = 0; l <= SINGLETON_BRA_LIMIT; l++) {
						SINGLETONS[i][j][k][l] = new Fix(i, j, k, l);
					}
				}
			}
		}
	}

	/**
	 * Empty line without instructions or branches.
	 */
	public static final LineImpl EMPTY = SINGLETONS[0][0][0][0];

	private static LineImpl getInstance(final CounterImpl instructions,
			final CounterImpl branches) {
		final int im = instructions.getMissedCount();
		final int ic = instructions.getCoveredCount();
		final int bm = branches.getMissedCount();
		final int bc = branches.getCoveredCount();
		if (im <= SINGLETON_INS_LIMIT && ic <= SINGLETON_INS_LIMIT
				&& bm <= SINGLETON_BRA_LIMIT && bc <= SINGLETON_BRA_LIMIT) {
			return SINGLETONS[im][ic][bm][bc];
		}
		return new Var(instructions, branches);
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
				final ICounter branches) {
			this.instructions = this.instructions.increment(instructions);
			this.branches = this.branches.increment(branches);
			return this;
		}
	}

	/**
	 * Immutable version.
	 */
	private static final class Fix extends LineImpl {
		public Fix(final int im, final int ic, final int bm, final int bc) {
			super(CounterImpl.getInstance(im, ic),
					CounterImpl.getInstance(bm, bc));
		}

		@Override
		public LineImpl increment(final ICounter instructions,
				final ICounter branches) {
			return getInstance(this.instructions.increment(instructions),
					this.branches.increment(branches));
		}
	}

	/** instruction counter */
	protected CounterImpl instructions;

	/** branch counter */
	protected CounterImpl branches;

	protected int coveredBranches;

	private LineImpl(final CounterImpl instructions,
			final CounterImpl branches) {
		this.instructions = instructions;
		this.branches = branches;
	}

	/**
	 * Adds the given counters to this line.
	 *
	 * @param instructions
	 *            instructions to add
	 * @param branches
	 *            branches to add
	 * @return instance with new counter values
	 */
	public abstract LineImpl increment(final ICounter instructions,
			final ICounter branches);

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
	 * See
	 * https://github.com/JetBrains/intellij-community/blob/5a47e55ec0769563a22fe3bbd5b52212673db9c0/plugins/coverage/src/com/intellij/coverage/JavaCoverageEngine.java#L612-L620
	 *
	 * TODO use own interface instead of BitSet?
	 *
	 * @return covered branches in the order of bytecode traversal
	 */
	public BitSet getCoveredBranches() {
		// FIXME also should return null when not available
		// ie in classes and when no branches
		if (getBranchCounter()
				.getTotalCount() > /* FIXME limited by storage */ 4) {
			return null;
		}
		final BitSet result = new BitSet(branches.getTotalCount());
		for (int i = 0; i < branches.getTotalCount(); i++) {
//			result.set(i, get(coveredBranches, i));
		}
		return result;
	}

	private static boolean get(final int bitSet, final int i) {
		return (bitSet & (1 << i)) != 0;
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
