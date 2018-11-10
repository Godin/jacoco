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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * java -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal -version | grep MaxInlineLevel
 *      intx MaxInlineLevel                            = 9                                   {product}
 * java version "1.8.0_131"
 * Java(TM) SE Runtime Environment (build 1.8.0_131-b11)
 * Java HotSpot(TM) 64-Bit Server VM (build 25.131-b11, mixed mode)
 * </pre>
 *
 * <pre>
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -XX:MaxInlineLevel=1
 *
 * &#64; 15   org.jacoco.Inlining::benchmark (4 bytes)   force inline by CompilerOracle
 *   &#64; 0   org.jacoco.Inlining::level1 (4 bytes)   inline (hot)
 *     &#64; 0   org.jacoco.Inlining::level2 (4 bytes)   inlining too deep
 *
 * benchmark  avgt    5  1.861 ± 0.018  ns/op
 * </pre>
 *
 * <pre>
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -XX:MaxInlineLevel=2
 *
 * &#64; 15   org.jacoco.Inlining::benchmark (4 bytes)   force inline by CompilerOracle
 *   &#64; 0   org.jacoco.Inlining::level1 (4 bytes)   inline (hot)
 *     &#64; 0   org.jacoco.Inlining::level2 (4 bytes)   inline (hot)
 *       &#64; 0   org.jacoco.Inlining::level3 (1 bytes)   inlining too deep
 *
 * benchmark  avgt    5  1.859 ± 0.004  ns/op
 * </pre>
 *
 * <pre>
 * benchmark  avgt    5  0.316 ± 0.006  ns/op
 * call       avgt    5  1.922 ± 0.058  ns/op
 * nop        avgt    5  0.310 ± 0.001  ns/op
 * </pre>
 */
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
@State(Scope.Thread)
public class MaxInlineLevel {

	@Benchmark
	public void nop() {
	}

	@Benchmark
	public void benchmark() {
		level1();
	}

	private static void level1() {
		level2();
	}

	private static void level2() {
		level3();
	}

	private static void level3() {
	}

	@Benchmark
	public void call() {
		dontInline();
	}

	@CompilerControl(CompilerControl.Mode.DONT_INLINE)
	private void dontInline() {
	}

	public static void main(String[] args) throws RunnerException {
		new Runner(new OptionsBuilder() //
				.include(MaxInlineLevel.class.getName() + ".*") //
				.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining") //
				.jvmArgsAppend("-XX:MaxInlineLevel=9") //
				.build()).run();
	}

}
