package org.jacoco.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.DTraceAsmProfiler;
import org.openjdk.jmh.profile.LinuxPerfNormProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class MyBenchmark {

	private static final int OPERATIONS = 10000;

	/// ~17 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
//	@Benchmark
	public void nop() {
	}

	/// ~33 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void call() {
		boolean[] local = b;
		target(local, 1);
	}

	/// ~39 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void call2() {
		boolean[] local = b;
		target(local, 1);
		target(local, 2);
	}

	private static final boolean[] b = new boolean[10];

	/// ~49 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void call3() {
		boolean[] local = b;
		target(local, 1);
		target(local, 2);
		target(local, 3);
	}

	/// ~10 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	@OperationsPerInvocation(OPERATIONS)
	public void calls() {
		boolean[] local = b;
		for (int i = 0; i < OPERATIONS; i++) {
			target(local, 1);
		}
	}

	/// ~20 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void noCall() {
		boolean[] local = b;
		local[1] = true; // 3 ns
	}

	/// ~23 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void noCall2() {
		boolean[] local = b;
		local[1] = true; // 3 ns
		local[2] = true; // 3 ns
	}

	/// ~26 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void noCall3() {
		boolean[] local = b;
		local[1] = true;
		local[2] = true;
		local[3] = true;
	}

	/// ~4 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
//	@Benchmark
	@OperationsPerInvocation(OPERATIONS)
	public void noCalls() {
		boolean[] local = b;
		for (int i = 0; i < OPERATIONS; i++) {
			local[1] = true;
		}
	}

	/// read from local[0] while write local[>0] to not measure only IFs
	/// ~32 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void branch3() {
		boolean[] local = b;
		if (!local[0])
			local[1] = true;
		if (!local[0])
			local[2] = true;
		if (!local[0])
			local[3] = true;
	}

	/// ~2.8 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
//	@Benchmark
	@OperationsPerInvocation(OPERATIONS)
	public void loop() {
		boolean[] local = b;
		for (int i = 0; i < OPERATIONS; i++) {
		}
	}

	/// ~7 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
//	@Benchmark
	@OperationsPerInvocation(OPERATIONS)
	public void branches_miss() {
		boolean[] local = b;
		for (int i = 0; i < OPERATIONS; i++) {
			if (!local[0])
				local[1] = true;
		}
	}

	/// ~5 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
//	@Benchmark
	@OperationsPerInvocation(OPERATIONS)
	public void branches_hit() {
		boolean[] local = b;
		for (int i = 0; i < OPERATIONS; i++) {
			if (!local[1])
				local[1] = true;
		}
	}

	/// ~52 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	// @Benchmark
	public void callBranch3() {
		boolean[] local = b;
		targetBranch(local, 1);
		targetBranch(local, 2);
		targetBranch(local, 3);
	}

	/// ~11 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
//	@Benchmark
	@OperationsPerInvocation(OPERATIONS)
	public void callBranches_miss() {
		boolean[] local = b;
		for (int i = 0; i < OPERATIONS; i++) {
			targetBranch(local, 1);
		}
	}

	/// ~11 ns/op
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	@Benchmark
	@OperationsPerInvocation(OPERATIONS)
	public void callBranches_hit() {
		boolean[] local = b;
		for (int i = 0; i < OPERATIONS; i++) {
			targetBranchHit(local, 1);
		}
	}

	private static void target(boolean[] b, int index) {
		b[index] = true;
	}

	private static void targetBranch(boolean[] b, int index) {
		if (b != null && !b[0])
			b[index] = true;
	}

	private static void targetBranchHit(boolean[] b, int index) {
		if (b != null && !b[index])
			b[index] = true;
	}

	public static void main(String[] args) throws Exception {
		new Runner(new OptionsBuilder() //
				.forks(1) //
				.warmupIterations(3) //
				.measurementIterations(3) //
				.mode(Mode.AverageTime) //
//				.addProfiler(XCTraceNormProfiler.class)
				.include(MyBenchmark.class.getName()) //
				.build()).run();
	}

}
