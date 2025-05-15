package org.jacoco.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class InitBenchmark {
	private static boolean[] probes;

	private int x;

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	public void nop() {
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	public int original() {
		return x;
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	public int instrumented_interpreted_init() {
		boolean[] local = interpreted_init();
		return x;
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	public int instrumented_compiled_init() {
		boolean[] local = compiled_init();
		return x;
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public boolean[] interpreted_init() {
		if (probes == null) {
			probes = new boolean[1];
		}
		return probes;
	}

	@Benchmark
	public boolean[] compiled_init() {
		if (probes == null) {
			probes = new boolean[1];
		}
		return probes;
	}

	public static void main(String[] args) throws RunnerException {
		new Runner(new OptionsBuilder() //
				.forks(1) //
				.warmupIterations(3) //
				.measurementIterations(3) //
				.mode(Mode.AverageTime) //
				// .addProfiler(XCTraceNormProfiler.class)
				.include(InitBenchmark.class.getName()) //
				.build()).run();
	}
}
