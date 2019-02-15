/*****************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jacoco.core.test.InstrumentingLoader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class Example {

	private static void nop() {
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public static class Target implements Callable {
		public Object call() {
			nop();
			nop();
			nop();
			nop();
			nop();
			return Object.class;
		}
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public static class HW implements Callable {
		private static boolean[] data;

		public Object call() {
			boolean[] probes = init();
			nop();
			probes[0] = true;
			nop();
			probes[1] = true;
			nop();
			probes[2] = true;
			nop();
			probes[3] = true;
			nop();
			probes[4] = true;
			return Object.class;
		}

		private static boolean[] init() {
			boolean[] data = HW.data;
			if (data == null) {
				data = new boolean[5];
				HW.data = data;
				return data;
			}
			return data;
		}
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public static class Opt implements Callable {
		private static Object data = new boolean[5];

		public Object call() {
			boolean[] probes = (boolean[]) data;
			nop();
			probes[0] = true;
			nop();
			probes[1] = true;
			nop();
			probes[2] = true;
			nop();
			probes[3] = true;
			nop();
			probes[4] = true;
			return Object.class;
		}
	}

	@CompilerControl(CompilerControl.Mode.EXCLUDE)
	public static class Hit implements Callable {
		private static Object data = new boolean[5];

		public Object call() {
			boolean[] probes = (boolean[]) data;
			nop();
			hit(probes, 0);
			nop();
			hit(probes, 1);
			nop();
			hit(probes, 2);
			nop();
			hit(probes, 3);
			nop();
			hit(probes, 4);
			return Object.class;
		}

		private void hit(boolean[] probes, int probeId) {
			if (!probes[probeId])
				probes[probeId] = true;
		}
	}

	private Callable original;
	private Callable opt;
	private Callable hit;
	private Callable hw;
	private Callable instrumented;

	@Setup
	public void setup() throws Exception {
		InstrumentingLoader loader = new InstrumentingLoader(Target.class);
		original = new Target();
		opt = new Opt();
		hit = new Hit();
		hw = new HW();
		instrumented = (Callable) loader.loadClass(Target.class.getName())
				.newInstance();
	}

	@Benchmark
	public Object original() throws Exception {
		return original.call();
	}

	@Benchmark
	public Object opt() throws Exception {
		return opt.call();
	}

	@Benchmark
	public Object hit() throws Exception {
		return hit.call();
	}

	@Benchmark
	public Object hw() throws Exception {
		return hw.call();
	}

//	@Benchmark
//	public Object instrumented() throws Exception {
//		return instrumented.call();
//	}

	public static void main(String[] args) throws RunnerException {
		Options options = new OptionsBuilder().include(Example.class.getName())
				.timeUnit(TimeUnit.NANOSECONDS).mode(Mode.AverageTime) //
				.forks(1) //
				.warmupIterations(5) //
				.measurementIterations(5) //
				.build();
		new Runner(options).run();
	}

}
