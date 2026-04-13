package org.jacoco.core.test.validation.java8;

import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Test;
import org.openjdk.jol.datamodel.Model32;
import org.openjdk.jol.datamodel.Model64;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.HotSpotLayouter;
import org.openjdk.jol.layouters.Layouter;

public class MemoryTest {
	/**
	 * instructions limit 8, branches limit 4 = ~68 Kb
	 */
	private Object old(final int insLimit, final int braLimit) {
		final LineImpl[][][][] singletons = new LineImpl[insLimit + 1][][][];
		for (int mi = 0; mi <= insLimit; mi++) {

			singletons[mi] = new LineImpl[insLimit + 1][][];
			for (int ci = 0; ci <= insLimit; ci++) {

				singletons[mi][ci] = new LineImpl[braLimit + 1][];
				for (int mb = 0; mb <= braLimit; mb++) {

					singletons[mi][ci][mb] = new LineImpl[braLimit + 1];
					for (int cb = 0; cb <= braLimit; cb++) {
						singletons[mi][ci][mb][cb] = new Fix(mi, ci, mb, cb);
					}
				}
			}
		}
		return singletons;
	}

	private Object totals(final int insLimitTotal, final int braLimitTotal) {
		final LineImpl[][][][] s;
		s = new LineImpl[insLimitTotal + 1][][][];
		for (int ti = 0; ti <= insLimitTotal; ti++) {

			s[ti] = new LineImpl[insLimitTotal + 1][][];
			for (int ci = 0; ci <= ti; ci++) {

				s[ti][ci] = new LineImpl[braLimitTotal + 1][];
				for (int tb = 0; tb <= braLimitTotal; tb++) {

					s[ti][ci][tb] = new LineImpl[braLimitTotal + 1];
					for (int cb = 0; cb <= tb; cb++) {

						s[ti][ci][tb][cb] = new Fix(ti - ci, ci, tb - cb, cb);
					}
				}
			}
		}
		return s;
	}

	private int cb(final int tb, final int bitMask) {
		int cb = 0;
		for (int b = 0; b < tb; b++) {
			if (org.jacoco.core.internal.analysis.LineImpl.get(bitMask, b)) {
				cb++;
			}
		}
		return cb;
	}

	/**
	 * permutations for
	 *
	 * <pre>
	 * 2 branches = 00, 10, 01, 11 = 2^2 = 4
	 * 4 branches = 2^4 = 16
	 * 6 branches = 2^6 = 64
	 * 8 branches = 2^8 = 256
	 * </pre>
	 *
	 * 4 branches also include values for 2 branches as 2 lower bits:
	 *
	 * <pre>
	 * 0000
	 * 0100
	 * 1000
	 * 1100
	 * 0010
	 * ...
	 * </pre>
	 *
	 * total * mask
	 *
	 * <pre>
	 * 0 = 1
	 * 2 = 4
	 * 4 = 16
	 * 6 = 64
	 * 8 = 256
	 * </pre>
	 *
	 * 2 branches = most likely 2 instructions eg ALOAD+IF
	 */
	private LineImpl[][][][] bitMask(final int instructionsTotalLimit,
			final int branchesTotalLimit) {
		// for (Object i : foo("", "").array) // 20 instructions
		// if (a != null && b != null && c != null) // 6 branches

		final LineImpl[][][][] s = new LineImpl[branchesTotalLimit / 2
				+ 1][][][];
		for (int tb = 0; tb <= branchesTotalLimit; tb += 2) {

			s[tb / 2] = new LineImpl[2 << tb + 1][][];
			for (int bitMask = 0; bitMask < 2 << tb; bitMask++) {

				s[tb / 2][bitMask] = new LineImpl[instructionsTotalLimit - tb
						+ 1][];
				for (int ti = tb; ti <= instructionsTotalLimit; ti += 1) {

					s[tb / 2][bitMask][ti
							- tb] = new LineImpl[instructionsTotalLimit + 1];
					for (int ci = 0; ci <= ti; ci++) {

						final int cb = cb(tb, bitMask);

						final int mi = ti - ci;
						final int mb = tb - cb;
						if (mi < 0 || mb < 0
								|| s[tb / 2][bitMask][ti - tb][ci] != null) {
							System.err.println("ti: " + ti + " mi: " + mi
									+ " ci: " + ci + " tb: " + tb + " mb: " + mb
									+ " cb: " + cb);
							throw new IllegalStateException();
						}
						s[tb / 2][bitMask][ti - tb][ci] = new Fix(mi, ci, mb, cb);
					}
				}
			}
		}
		return s;
	}

	private LineImpl[][][][] experiment(final int instructionsTotalLimit,
			final int branchesTotalLimit) {
		final LineImpl[][][][] s = new LineImpl[branchesTotalLimit / 2
				+ 1][][][];
		for (int tb = 0; tb <= branchesTotalLimit; tb += 2) {

			s[tb / 2] = new LineImpl[instructionsTotalLimit + 1][][];
			for (int ti = tb; ti <= instructionsTotalLimit; ti += 1) {

				s[tb / 2][ti - tb] = new LineImpl[instructionsTotalLimit + 1][];
				for (int ci = 0; ci <= ti; ci++) {

					s[tb / 2][ti - tb][ci] = new LineImpl[2 << tb + 1];
					for (int bitMask = 0; bitMask < 2 << tb; bitMask++) {

						final int cb = cb(tb, bitMask);
						final int mi = ti - ci;
						final int mb = tb - cb;
						s[tb / 2][ti - tb][ci][bitMask] = new Fix(mi, ci, mb, cb);
					}
				}
			}
		}
		return s;
	}

	static class LineImpl {
		/** instruction counter */
		protected CounterImpl instructions;
		/** branch counter */
		protected CounterImpl branches;

		/**
		 * 24 bytes instance without this field
		 *
		 * int = +0 with compressed oops +8 without
		 *
		 * long = +8 with compressed oops +8 without
		 */
		int bitMask;
		// long bitMask;
	}

	static class Fix extends LineImpl {
		public Fix() {
		}

		public Fix(final int mi, final int ci, final int mb, final int cb) {
			this.instructions = CounterImpl.getInstance(mi, ci);
			this.branches = CounterImpl.getInstance(mb, cb);
			this.bitMask = 0;
		}
	}

	static class Var extends LineImpl {
		int bitSet;
	}

	private static final Layouter layouter32 = new HotSpotLayouter(
			new Model32(), 8);

	private static final Layouter layouter64_coops = new HotSpotLayouter(
			new Model64(true, true), 8);

	private static final Layouter layouter64 = new HotSpotLayouter(
			new Model64(false, false), 8);

	private static final Layouter layouter = new CurrentLayouter();

	@Test
	public void test() {

		System.out.println(layouter64_coops);
		System.out.println(ClassLayout
				.parseClass(LineImpl.class, layouter64_coops).toPrintable());
		System.out.println(layouter64);
		System.out.println(
				ClassLayout.parseClass(Var.class, layouter64).toPrintable());
		System.out.println(layouter32);
		System.out.println(ClassLayout.parseClass(LineImpl.class, layouter32)
				.toPrintable());

		System.out.println(layouter);
		System.out.println(
				ClassLayout.parseClass(Var.class, layouter).toPrintable());

		System.out.println("Old (8, 4):");
		footprint(old(8, 4));

		System.out.println("Singletons total (16, 8):");
		footprint(totals(16, 8));

		System.out.println("BitMask:");
		footprint(bitMask(16, 4));

		System.out.println("Experiment:");
		footprint(experiment(16, 4));
	}

	private void footprint(final Object o) {
		System.out.println(GraphLayout.parseInstance(o).toFootprint());
		long totalSize = GraphLayout.parseInstance(o).totalSize();
		System.out.println(totalSize + " bytes");
		System.out.println((totalSize / 1024.0) + " kb");
	}

}
