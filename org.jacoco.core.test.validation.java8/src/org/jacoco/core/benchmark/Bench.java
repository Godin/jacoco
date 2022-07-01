/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class Bench {

	private Runtime runtime;

	static class Runtime {
		@CompilerControl(CompilerControl.Mode.DONT_INLINE)
		@Override
		public boolean equals(Object obj) {
			Object[] a = (Object[]) obj;
			long classId = (long) a[0];
			String className = (String) a[1];
			int probes = (int) a[2];
			a[0] = get(classId, className, probes);
			return false;
		}

		@CompilerControl(CompilerControl.Mode.DONT_INLINE)
		public boolean[] direct(long classId, String className, int probes) {
			synchronized (this) {
				return new boolean[probes];
			}
		}

		@CompilerControl(CompilerControl.Mode.DONT_INLINE)
		private boolean[] get(long classId, String className, int probes) {
			synchronized (this) {
				return new boolean[probes];
			}
		}
	}

	@Setup
	public void setup() {
		runtime = new Runtime();
	}

	// @Benchmark
	// public void nop(Blackhole bh) {
	// }

	@Benchmark
	public void direct(Blackhole bh) {
		boolean[] probes = runtime.direct(42, "className", 42);
		bh.consume(probes);
	}

	@Benchmark
	public void current(Blackhole bh) {
		Object[] a = new Object[3];
		a[0] = 42L;
		a[1] = "className";
		a[2] = 42;
		runtime.equals(a);
		boolean[] probes = (boolean[]) a[0];
		bh.consume(probes);
	}

	public static void main(String[] args) throws RunnerException {
		Options options = new OptionsBuilder() //
				.include(Bench.class.getName()) //
				.forks(1) //
				.warmupIterations(10) //
				.measurementIterations(10) //
				.mode(Mode.AverageTime) //
				.mode(Mode.SingleShotTime) //
				// .mode(Mode.SampleTime)
				.timeUnit(TimeUnit.NANOSECONDS) //
				.build();
		new Runner(options).run();
	}

}
