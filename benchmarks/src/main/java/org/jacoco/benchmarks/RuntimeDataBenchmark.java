/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.benchmarks;

import org.jacoco.core.runtime.RuntimeData;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Fork(1)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class RuntimeDataBenchmark {

	@Param
	RuntimeDataFactory runtimeData;

	private RuntimeData data;

	@Setup
	public void setup() {
		data = runtimeData.create();
	}

	@Benchmark
	public boolean[] getProbes() {
		final Object[] args = new Object[] { DataAccessor.CLASS_ID,
				DataAccessor.CLASS_NAME, DataAccessor.PROBES_COUNT };
		data.equals(args);
		return (boolean[]) args[0];
	}

	public static void main(String[] args) throws RunnerException {
		final int availableProcessors = Runtime.getRuntime().availableProcessors();

		final StringBuilder summary = new StringBuilder();
		for (int threads = 1; threads <= availableProcessors; threads *= 2) {
			final Options options = new OptionsBuilder()
					.include(RuntimeDataBenchmark.class.getName())
					.param("runtimeData",
							RuntimeDataFactory.MOCK.name(),
							RuntimeDataFactory.CURRENT.name(),
							RuntimeDataFactory.CONCURRENT_HASH_MAP.name(),
							RuntimeDataFactory.ARRAY.name(),
							RuntimeDataFactory.HASH_MAP.name())
					.threads(threads)
					.build();
			final Collection<RunResult> results = new Runner(options).run();
			System.out.println();

			summary.append(String.format("%d threads\n", threads));
			for (final RunResult result : results) {
				final String name = result.getParams().getParam("runtimeData");
				final double score = result.getPrimaryResult().getScore();
				final double scoreError = result.getPrimaryResult().getStatistics()
						.getMeanErrorAt(0.99);
				final String scoreUnit = result.getPrimaryResult().getScoreUnit();
				summary.append(String.format("%20s: %8.3f ± %6.3f %s\n", name, score,
						scoreError, scoreUnit));
			}
			summary.append("\n");
		}

		System.out.println(System.getProperty("java.runtime.name") + ", "
				+ System.getProperty("java.runtime.version"));
		System.out.println(System.getProperty("java.vm.name") + ", "
				+ System.getProperty("java.vm.version"));
		System.out.println(System.getProperty("os.name") + ", "
				+ System.getProperty("os.version") + ", "
				+ System.getProperty("os.arch"));
		System.out.println(availableProcessors + " available processors");
		System.out.println();
		System.out.println(summary.toString());
	}

}
