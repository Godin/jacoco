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
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 5)
public class Interpreter {

	private static boolean[] jacocoData;

	/**
	 * All instrumented classes must have their copy of this method, therefore
	 * there is no guarantee that it will be compiled.
	 *
	 * Call site:
	 *
	 * <pre>
	 *   push id      // 1-3 bytes
	 *   invokestatic // 3
	 * </pre>
	 */
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	private static void hit_not_compiled(int id) {
		boolean[] probes = Interpreter.jacocoData;
		if (probes != null && !probes[id])
			probes[id] = true;
	}

	/**
	 * All instrumented classes can use this method, therefore likely that it
	 * will be compiled. However in this case either {@link #jacocoInit()}
	 * should be placed in instrumented method, or precision in case of "bad
	 * cycles" should be sacrificed, because this method can't initialize
	 * {@link #jacocoData}.
	 *
	 * Call site:
	 *
	 * <pre>
	 *   aload var    // 1-2 bytes
	 *   push  id     // 1-3
	 *   invokestatic // 3
	 * </pre>
	 */
	private static void hit_compiled(boolean[] probes, int id) {
		if (probes != null && !probes[id])
			probes[id] = true;
	}

	/**
	 * All instrumented classes must have their copy of this method, therefore
	 * there is no guarantee that it will be compiled.
	 */
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	private static boolean[] jacocoInit() {
		if (jacocoData == null) {
			jacocoData = new boolean[8];
		}
		return jacocoData;
	}

	// 1 probe

	/**
	 * Each current probe
	 *
	 * <pre>
	 *   aload var // 1-2 bytes
	 *   push id   // 1-3
	 *   iconst 1  // 1
	 *   bastore   // 1
	 * </pre>
	 */
	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_1_hit_inlined() {
		boolean[] probes = jacocoInit();
		probes[0] = true;
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_1_hit_not_compiled() {
		hit_not_compiled(0);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_1_hit_compiled() {
		boolean[] probes = Interpreter.jacocoData;
		hit_compiled(probes, 0);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_1_hit_compiled_precise_bad_cycle() {
		boolean[] probes = jacocoInit();
		hit_compiled(probes, 0);
	}

	// 2 probes

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_2_hit_inlined() {
		boolean[] probes = jacocoInit();
		probes[0] = true;
		probes[1] = true;
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_2_hit_not_compiled() {
		hit_not_compiled(0);
		hit_not_compiled(1);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_2_hit_compiled() {
		boolean[] probes = Interpreter.jacocoData;
		hit_compiled(probes, 0);
		hit_compiled(probes, 1);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_2_hit_compiled_precise_bad_cycle() {
		boolean[] probes = jacocoInit();
		hit_compiled(probes, 0);
		hit_compiled(probes, 1);
	}

	// 4 probes

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_4_hit_inlined() {
		boolean[] probes = jacocoInit();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_4_hit_not_compiled() {
		hit_not_compiled(0);
		hit_not_compiled(1);
		hit_not_compiled(2);
		hit_not_compiled(3);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_4_hit_compiled() {
		boolean[] probes = Interpreter.jacocoData;
		hit_compiled(probes, 0);
		hit_compiled(probes, 1);
		hit_compiled(probes, 2);
		hit_compiled(probes, 3);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_4_hit_compiled_precise_bad_cycle() {
		boolean[] probes = jacocoInit();
		hit_compiled(probes, 0);
		hit_compiled(probes, 1);
		hit_compiled(probes, 2);
		hit_compiled(probes, 3);
	}

	// 8 probes

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_8_hit_inlined() {
		boolean[] probes = jacocoInit();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
		probes[4] = true;
		probes[5] = true;
		probes[6] = true;
		probes[7] = true;
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_8_hit_not_compiled() {
		hit_not_compiled(0);
		hit_not_compiled(1);
		hit_not_compiled(2);
		hit_not_compiled(3);
		hit_not_compiled(4);
		hit_not_compiled(5);
		hit_not_compiled(6);
		hit_not_compiled(7);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_8_hit_compiled() {
		boolean[] probes = Interpreter.jacocoData;
		hit_compiled(probes, 0);
		hit_compiled(probes, 1);
		hit_compiled(probes, 2);
		hit_compiled(probes, 3);
		hit_compiled(probes, 4);
		hit_compiled(probes, 5);
		hit_compiled(probes, 6);
		hit_compiled(probes, 7);
	}

	@Benchmark
	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public void probes_8_hit_compiled_precise_bad_cycle() {
		boolean[] probes = jacocoInit();
		hit_compiled(probes, 0);
		hit_compiled(probes, 1);
		hit_compiled(probes, 2);
		hit_compiled(probes, 3);
		hit_compiled(probes, 4);
		hit_compiled(probes, 5);
		hit_compiled(probes, 6);
		hit_compiled(probes, 7);
	}

	public static void main(String[] args) throws RunnerException {
		Collection<RunResult> results = new Runner(new OptionsBuilder() //
				.include(Interpreter.class.getName() + ".*8.*") //
				.jvmArgs( //
						"-XX:+UnlockDiagnosticVMOptions" //
				// , "-XX:+PrintTieredEvents" //
				) //
				.build()).run();

		ResultFormatFactory.getInstance(ResultFormatType.JSON, "/tmp/jmh.json")
				.writeOut(results);
	}

}
