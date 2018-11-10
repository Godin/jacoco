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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
public class Getter {

	private static Object jacocoAccess = new RuntimeData();

	private static boolean[] jacocoData;

	private static boolean[] jacocoInit() {
		boolean[] probes = jacocoData;
		if (probes == null) {
			Object[] args = new Object[] { //
					42L, // classId
					"Foo", // className
					100 // probeCount
			};
			jacocoAccess.equals(args);
			probes = (boolean[]) args[0];
			jacocoData = probes;
		}
		return probes;
	}

	@Setup
	public void setup() {
		jacocoData = jacocoInit();
	}

	private int x = 42;

	@Benchmark
	public int original() {
		return x;
	}

	@Benchmark
	public int simulated_current() {
		boolean[] probes = jacocoInit();
		probes[0] = true;
		return x;
	}

	@Benchmark
	public int simulated_proposed() {
		proposed(0);
		return x;
	}

	private static void proposed(int id) {
		if (jacocoData != null && !jacocoData[id])
			jacocoData[id] = true;
	}

	@Benchmark
	public int simulated_proposed_no_check() {
		proposed_no_check(0);
		return x;
	}

	private static void proposed_no_check(int id) {
		if (!jacocoData[id])
			jacocoData[id] = true;
	}

	@Benchmark
	public int simulated_proposed_catch() {
		proposed_catch(0);
		return x;
	}

	private static void proposed_catch(int id) {
		try {
			if (!jacocoData[id])
				jacocoData[id] = true;
		} catch (NullPointerException ignored) {
		}
	}

	@Benchmark
	public int simulated_proposed_loop() {
		proposed_loop(0);
		return x;
	}

	private static void proposed_loop(int id) {
		while (true) {
			try {
				if (!jacocoData[id])
					jacocoData[id] = true;
				return;
			} catch (NullPointerException e) {
				jacocoInit();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Runner(new OptionsBuilder() //
				.include(Getter.class.getName() + ".*") //
				.jvmArgs("-XX:TieredStopAtLevel=1") // C1
				.build()).run();
	}

	static void asm(byte[] bytes) {
		TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null,
				new ASMifier(), new PrintWriter(System.out));
		new ClassReader(bytes).accept(traceClassVisitor, 0);
	}

}
