package org.jacoco.core;

import org.openjdk.jmh.annotations.Benchmark;
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
public class B {

	private static boolean[] probes;

	private byte[] bytes;

	@Setup
	public void setup() {
		bytes = new byte[128];
		probes = new boolean[2];
	}

	@Benchmark
	public void original(Blackhole blackhole) {
		for (byte b : bytes) {
			blackhole.consume(b);
		}
	}

	@Benchmark
	public void local(Blackhole blackhole) {
		boolean[] probes = B.probes;
		for (byte b : bytes) {
			blackhole.consume(b);
			probes[1] = true;
		}
	}

	@Benchmark
	public void field(Blackhole blackhole) {
		for (byte b : bytes) {
			blackhole.consume(b);
			probes[1] = true;
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options options = new OptionsBuilder().include(B.class.getName()) //
				.timeUnit(TimeUnit.NANOSECONDS) //
				.mode(Mode.AverageTime) //
				.forks(1) //
				.warmupIterations(5) //
				.measurementIterations(5) //
				.build();
		new Runner(options).run();
	}

}
