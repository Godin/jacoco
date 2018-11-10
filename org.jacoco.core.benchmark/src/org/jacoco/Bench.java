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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class Bench {

	int x;

	static boolean[] probes;

	@Setup
	public void setup() {
		probes = new boolean[100];
	}

	private static boolean[] init() {
		if (probes == null) {
			probes = new boolean[100];
		}
		return probes;
	}

	@Benchmark
	public int original() {
		return x;
	}

	@Benchmark
	public int simulated_current() {
		boolean[] probes = init();
		probes[0] = true;
		return x;
	}

//	@Benchmark
//	public int simulated_proposed_branch() {
//		boolean[] probes = init();
//		if (probes[0] == false)
//			probes[0] = true;
//		return x;
//	}

//	@Benchmark
//	public int simulated_proposed_branch_in_method() {
//		init();
//		hit(0);
//		return x;
//	}

	@Benchmark
	public int simulated_more() {
		hit(0);
		return x;
	}

	private static void hit(int id) {
		if (probes != null && probes[id] == false)
			probes[id] = true;
	}

	public static void main(String[] args) throws RunnerException {
		List<Collection<RunResult>> results = new ArrayList<>();

		int start = 1;
		int end = 1;

		for (int threads = start; threads <= end; threads *= 2) {
			System.out.println();
			System.out.println(threads + " threads");

			results.add(new Runner(new OptionsBuilder() //
					.forks(1) //
					.jvmArgs("-XX:TieredStopAtLevel=1") //
					.mode(Mode.AverageTime) //
					// .mode(Mode.SingleShotTime) //
					.timeUnit(TimeUnit.NANOSECONDS) //
					.warmupIterations(5) //
					.measurementIterations(5) //
					.threads(threads) //
					.build()).run());
		}

		int threads = start;
		for (Collection<RunResult> result : results) {
			System.out.println();
			System.out.println(threads + " threads");

			threads *= 2;
			ResultFormatFactory.getInstance(ResultFormatType.TEXT, System.out)
					.writeOut(result);
		}
	}

}
