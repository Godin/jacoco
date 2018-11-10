/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco;

import org.jacoco.core.runtime.RuntimeData;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class SimulateInlining {

	static Object jacocoAccess = new RuntimeData();

	static boolean[] jacocoData;

	private static boolean[] jacocoInit() {
		boolean[] probes = jacocoData;
		if (probes == null) {
			Object[] args = new Object[] { //
					42L, // classId
					"org/jacoco/Simulate", // name
					100 // probeCount
			};
			jacocoAccess.equals(args);
			probes = (boolean[]) args[0];
			jacocoData = probes;
		}
		return probes;
	}

	int x;

	@Setup
	public void setup() {
		jacocoInit();
	}

	// original

	@Benchmark
	public int original_depth_7() {
		return original_depth_6();
	}

	@Benchmark
	public int original_depth_6() {
		return original_depth_5();
	}

	@Benchmark
	public int original_depth_5() {
		return original_depth_4();
	}

	@Benchmark
	public int original_depth_4() {
		return original_depth_3();
	}

	@Benchmark
	public int original_depth_3() {
		return original_depth_2();
	}

	@Benchmark
	public int original_depth_2() {
		return original_depth_1();
	}

	@Benchmark
	public int original_depth_1() {
		return original_depth_0();
	}

	@Benchmark
	public int original_depth_0() {
		return x;
	}

	// simulated current

	@Benchmark
	public int simulated_current_depth_7() {
		boolean[] probes = jacocoInit();
		probes[6] = true;
		return simulated_current_depth_6();
	}

	@Benchmark
	public int simulated_current_depth_6() {
		boolean[] probes = jacocoInit();
		probes[6] = true;
		return simulated_current_depth_5();
	}

	@Benchmark
	public int simulated_current_depth_5() {
		boolean[] probes = jacocoInit();
		probes[5] = true;
		return simulated_current_depth_4();
	}

	@Benchmark
	public int simulated_current_depth_4() {
		boolean[] probes = jacocoInit();
		probes[4] = true;
		return simulated_current_depth_3();
	}

	@Benchmark
	public int simulated_current_depth_3() {
		boolean[] probes = jacocoInit();
		probes[3] = true;
		return simulated_current_depth_2();
	}

	@Benchmark
	public int simulated_current_depth_2() {
		boolean[] probes = jacocoInit();
		probes[2] = true;
		return simulated_current_depth_1();
	}

	@Benchmark
	public int simulated_current_depth_1() {
		boolean[] probes = jacocoInit();
		probes[1] = true;
		return simulated_current_depth_0();
	}

	@Benchmark
	public int simulated_current_depth_0() {
		boolean[] probes = jacocoInit();
		probes[0] = true;
		return x;
	}

	// simulated proposed

	@Benchmark
	public int simulated_proposed_depth_7() {
		jacocoHit(7);
		return simulated_proposed_depth_6();
	}

	@Benchmark
	public int simulated_proposed_depth_6() {
		jacocoHit(6);
		return simulated_proposed_depth_5();
	}

	@Benchmark
	public int simulated_proposed_depth_5() {
		jacocoHit(5);
		return simulated_proposed_depth_4();
	}

	@Benchmark
	public int simulated_proposed_depth_4() {
		jacocoHit(4);
		return simulated_proposed_depth_3();
	}

	@Benchmark
	public int simulated_proposed_depth_3() {
		jacocoHit(3);
		return simulated_proposed_depth_2();
	}

	@Benchmark
	public int simulated_proposed_depth_2() {
		jacocoHit(2);
		return simulated_proposed_depth_1();
	}

	@Benchmark
	public int simulated_proposed_depth_1() {
		jacocoHit(1);
		return simulated_proposed_depth_0();
	}

	@Benchmark
	public int simulated_proposed_depth_0() {
		jacocoHit(0);
		return x;
	}

	private static void jacocoHit(int id) {
		if (jacocoData != null && !jacocoData[id]) {
			jacocoData[id] = true;
		}
	}

	public static void main(String[] args)
			throws RunnerException, FileNotFoundException {
		Collection<RunResult> results = new Runner(new OptionsBuilder() //
				.include(SimulateInlining.class.getName() + ".*") //
				.jvmArgs("-XX:TieredStopAtLevel=1", "-XX:MaxInlineLevel=3") //
				.forks(1) //
				.timeUnit(TimeUnit.MICROSECONDS) //
				.mode(Mode.Throughput) //
				.warmupIterations(5) //
				.measurementIterations(5) //
				.build()).run();

		PrintStream out = new PrintStream(
				new FileOutputStream("/tmp/jmh.json"));
		ResultFormatFactory.getInstance(ResultFormatType.JSON, out)
				.writeOut(results);
		out.close();
	}

}
