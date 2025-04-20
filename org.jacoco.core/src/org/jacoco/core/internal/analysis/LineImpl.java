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

	private static final LineImpl[][][][] SINGLETONS = new LineImpl[SINGLETON_INS_LIMIT
			+ 1][][][];

	static {
		for (int it = 0; it <= SINGLETON_INS_LIMIT; it++) {
			SINGLETONS[it] = new LineImpl[SINGLETON_INS_LIMIT + 1][][];
			for (int ic = 0; ic <= it; ic++) {
				SINGLETONS[it][ic] = new LineImpl[SINGLETON_BRA_LIMIT + 1][];
				for (int bt = 0; bt <= SINGLETON_BRA_LIMIT; bt++) {
					SINGLETONS[it][ic][bt] = new LineImpl[SINGLETON_BRA_LIMIT
							+ 1];
					for (int bc = 0; bc <= bt; bc++) {
						SINGLETONS[it][ic][bt][bc] = new Fix(it - ic, ic,
								bt - bc, bc);
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
		final int it = instructions.getTotalCount();
		final int ic = instructions.getCoveredCount();
		final int bt = branches.getTotalCount();
		final int bc = branches.getCoveredCount();
		if (it <= SINGLETON_INS_LIMIT && bt <= SINGLETON_BRA_LIMIT) {
			return SINGLETONS[it][ic][bt][bc];
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
