package org.jacoco.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * https://github.com/openjdk/jdk/blob/master/src/hotspot/cpu/x86/templateTable_x86.cpp#L1005
 * vs
 * https://github.com/openjdk/jdk/blob/master/src/hotspot/cpu/x86/templateTable_x86.cpp#L1112
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ArrayStoreBenchmark {

	private static final int LOOP = 1000;
	int[] i = new int[10];
	boolean[] b = new boolean[10];

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	@OperationsPerInvocation(LOOP)
	public void nop() {
		for (int counter = 0; counter < LOOP; counter++) {
		}
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	@OperationsPerInvocation(LOOP)
	public void iastore() {
		for (int counter = 0; counter < LOOP; counter++) {
			i[0] = 1;
		}
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	@OperationsPerInvocation(LOOP)
	public void bastore() {
		for (int counter = 0; counter < LOOP; counter++) {
			b[0] = true;
		}
	}

	public static void main(String[] args) throws Exception {
		new Runner(new OptionsBuilder() //
				.forks(1) //
				.warmupIterations(3) //
				.measurementIterations(3) //
				.mode(Mode.AverageTime) //
				.include(ArrayStoreBenchmark.class.getName()) //
				.build()).run();
	}

}
