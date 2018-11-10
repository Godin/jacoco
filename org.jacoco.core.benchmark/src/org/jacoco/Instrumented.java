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

import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassFieldProbeArrayStrategy;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.NewStrategy;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.ModifiedSystemClassRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.test.TargetLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
@State(Scope.Thread)
public class Instrumented {

	public static class SimulatedSystemClass {
		public static Object jacocoAccess;
	}

	private static IRuntime runtime = new ModifiedSystemClassRuntime(
			SimulatedSystemClass.class, "jacocoAccess");

	public static abstract class BenchTarget {
		static boolean[] jacocoData;
		static Blackhole bh;

		static boolean[] jacocoInit() {
			boolean[] probes = jacocoData;
			if (probes == null) {
				Object[] args = new Object[] { //
						42L, // classId
						"Foo", // className
						100 // probeCount
				};
				SimulatedSystemClass.jacocoAccess.equals(args);
				probes = (boolean[]) args[0];
				jacocoData = probes;
			}
			return probes;
		}

		abstract public void run(Blackhole bh);

		static int x;

		public static void call() {
			bh.consume(x);
		}
	}

	private BenchTarget original = new Original();
	private BenchTarget instrumented;
	private BenchTarget proposed;

	private static final boolean DEBUG_ASM = false;

	private BenchTarget instrument(final IProbeArrayStrategy strategy)
			throws Exception {
		final byte[] source = TargetLoader.getClassDataAsBytes(Original.class);

		final ClassReader reader = new ClassReader(source);
		final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		final int version = InstrSupport.getVersionMajor(source);
		final ClassVisitor visitor = new ClassProbesAdapter(
				new ClassInstrumenter(strategy, writer),
				InstrSupport.needsFrames(version));
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);

		if (DEBUG_ASM) {
			TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null,
					new ASMifier(), new PrintWriter(System.out));
			new ClassReader(writer.toByteArray()).accept(traceClassVisitor, 0);
		}

		return (BenchTarget) new TargetLoader()
				.add(Original.class, writer.toByteArray()).newInstance();
	}

	@Setup
	public void setup(Blackhole bh) throws Exception {
		RuntimeData data = new RuntimeData();
		runtime.startup(data);

		final String className = Original.class.getName().replace('.', '/');

		instrumented = instrument(
				new ClassFieldProbeArrayStrategy(className, 0, true, runtime));

		proposed = instrument(
				new NewStrategy(false, className, 0, 100, true, runtime));

		simulated_current = new SimulatedCurrent();
		simulated_proposed = new SimulatedProposed();

		BenchTarget.bh = bh;
		BenchTarget.jacocoInit();
	}

	@Benchmark
	public void original(Blackhole bh) {
		original.run(bh);
	}

	@Benchmark
	public void instrumented_current(Blackhole bh) {
		instrumented.run(bh);
	}

	@Benchmark
	public void instrumented_proposed(Blackhole bh) {
		proposed.run(bh);
	}

	@Benchmark
	public void simulated_current(Blackhole bh) {
		simulated_current.run(bh);
	}

	private BenchTarget simulated_current;

	@Benchmark
	public void simulated_proposed(Blackhole bh) {
		simulated_proposed.run(bh);
	}

	private BenchTarget simulated_proposed;

	public static class Original extends BenchTarget {
		public void run(Blackhole bh) {
			call();
			call();
			call();
		}
	}

	public static class SimulatedCurrent extends BenchTarget {
		int x;

		@Override
		public void run(Blackhole bh) {
			boolean[] probes = jacocoInit();
			// no probe at the beginning of the methods even if first line is method call?
			call();
			probes[1] = true;
			call();
			probes[2] = true;
			call();
			probes[3] = true;
		}
	}

	public static class SimulatedProposed extends BenchTarget {
		private static boolean[] data = jacocoInit();

		int x;

		@Override
		public void run(Blackhole bh) {
			// no probe at the beginning of the methods even if first line is method call?
			call();
			hit(1);
			call();
			hit(2);
			call();
			hit(3);
		}

		private static void hit(int id) {
			boolean[] probes = data;
			if (probes != null && !probes[id])
				probes[id] = true;
		}
	}

	public static void main(String[] args) throws Exception {
		new Runner(new OptionsBuilder() //
				.include(Instrumented.class.getName() + ".*instrumented") //
				.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining") //
				// .jvmArgs("-XX:TieredStopAtLevel=0") // interpreter
				// .jvmArgs("-XX:TieredStopAtLevel=1") // C1
				// .jvmArgs("-XX:TieredStopAtLevel=4") // C2
				.build()).run();
	}

}
