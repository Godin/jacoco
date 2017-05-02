package org.jacoco.benchmark;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.CompilerProfiler;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.profile.HotspotRuntimeProfiler;
import org.openjdk.jmh.profile.PausesProfiler;
import org.openjdk.jmh.profile.SafepointsProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Fork(1)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class Bench {

	private final File file = new File(
			System.getProperty("java.home") + "/lib/rt.jar");

	@Benchmark
	public void bench() throws Exception {
		analyze(file);
	}

	@Warmup(iterations = 0)
	@Measurement(iterations = 1)
	@BenchmarkMode(Mode.SingleShotTime)
	@Benchmark
	public void single_shot() throws Exception {
		analyze(file);
	}

	private static final File file2 = new File(
			"/Users/evgeny.mandrikov/projects/jacoco/org.jacoco.core.test/target/classes/org/jacoco/core/test/filter/targets/Synchronized.class");

//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	@BenchmarkMode(Mode.Throughput)
//	@Benchmark
//	public void test() throws Exception {
//		analyze(file2);
//	}

	private static void analyze(final File file) throws IOException {
		new Analyzer(new ExecutionDataStore(), new CoverageBuilder())
				.analyzeAll(file);
	}

	public static void main(String[] args) throws Exception {
		Options options = new OptionsBuilder().include(Bench.class.getName())
//				.addProfiler(HotspotMemoryProfiler.class)
//				.addProfiler(HotspotRuntimeProfiler.class)
//				.addProfiler(SafepointsProfiler.class)
//				.addProfiler(CompilerProfiler.class)
//				.addProfiler(GCProfiler.class)
//				.addProfiler(PausesProfiler.class)
				.build();
		new Runner(options).run();
	}

}

//# VM version: JDK 1.7.0_80, VM 24.80-b11
//Benchmark          Mode  Cnt     Score     Error  Units
//Bench.bench        avgt    5  2655.746 ± 367.560  ms/op
//Bench.single_shot    ss       4794.511            ms/op
