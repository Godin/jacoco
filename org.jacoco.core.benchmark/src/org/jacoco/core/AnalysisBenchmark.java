/*******************************************************************************
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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.profile.Profiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class AnalysisBenchmark {

	@Param("/Users/evgeny.mandrikov/.java-select/versions/8/jre/lib/rt.jar")
	String input;

	private Analyzer analyzer;

	@Setup
	public void setup() throws Exception {
		analyzer = new Analyzer(new ExecutionDataStore(), new ICoverageVisitor() {
			public void visitCoverage(IClassCoverage coverage) {
			}
		});
	}

	@Benchmark
	public Object analyze() throws IOException {
		return analyzer.analyzeAll(new File(input));
	}

	public static void main(String[] args) throws RunnerException {
		Options options = new OptionsBuilder() //
				.include(AnalysisBenchmark.class.getName()) //
				// .jvmArgsAppend("-XX:CompileCommand=dontinline,Condy::init")
				// .jvmArgsAppend("-XX:CompileCommand=exclude,Condy::*")
				.forks(10) //
//				.warmupIterations(5) //
//				.measurementIterations(5) //
				.build(); //
		new Runner(options).run();

//		Benchmark                                                                         (input)  Mode  Cnt     Score     Error  Units
//		without ClassReader.SKIP_FRAMES
//		AnalysisBenchmark.analyze  /Users/evgeny.mandrikov/.java-select/versions/8/jre/lib/rt.jar    ss   10  5536.311 ± 520.116  ms/op
//		with ClassReader.SKIP_FRAMES
//		AnalysisBenchmark.analyze  /Users/evgeny.mandrikov/.java-select/versions/8/jre/lib/rt.jar    ss   10  5122.913 ± 409.779  ms/op
	}

}
