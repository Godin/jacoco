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

import org.jacoco.core.runtime.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class DataAccessorBenchmark {

	@Param
	RuntimeFactory runtime;

	@Param
	RuntimeDataFactory runtimeData;

	private IRuntime r;
	private DataAccessor dataAccessor;

	@Setup
	public void setup() throws Exception {
		final RuntimeData runtimeData = this.runtimeData.create();
		r = this.runtime.create();
		r.startup(runtimeData);
		dataAccessor = DataAccessor.createFor(r);
	}

	@TearDown
	public void tearDown() {
		r.shutdown();
	}

	@Benchmark
	public boolean[] getProbes() {
		return dataAccessor.getData();
	}

	public static void main(String[] args) throws RunnerException {
		Options options = new OptionsBuilder()
				.include(DataAccessorBenchmark.class.getName())
				.param("runtime",
						RuntimeFactory.ModifiedSystemClass.name(),
						RuntimeFactory.URLStreamHandlerRuntime.name(),
						RuntimeFactory.SystemPropertiesRuntime.name(),
						RuntimeFactory.LoggerRuntime.name())
				.param("runtimeData",
						RuntimeDataFactory.MOCK.name(),
						RuntimeDataFactory.CURRENT.name(),
						RuntimeDataFactory.CONCURRENT_HASH_MAP.name(),
						RuntimeDataFactory.ARRAY.name(),
						RuntimeDataFactory.HASH_MAP.name())
				.build();
		new Runner(options).run();
	}

}
