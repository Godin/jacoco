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

@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 1)
@State(Scope.Thread)
public class MaxInlineSize {

	int x;

	@Benchmark
	public void benchmark() {
		callee_is_too_large();
	}

	private void hit() {
	    // MinInliningThreshold - minimum invocation count a method needs to have to be inlined.
    }

    // MaxTrivialSize = 6
    private MaxInlineSize trivial() {
	    return this;
    }

    /**
     * <pre>
     * aload_0   // +1
     * dup       // +1
     * getfield  // +3
     * iconst_1  // +1
     * iadd      // +1
     * putfield  // +3
     * ...
     * return    // +1
     * </pre>
     */
    private void callee_is_too_large() {
        x++; // 11
        x++; // 21
        x++; // 31
        x++; // 41
    }

    private void inline() {
        x++; // 11
        x++; // 21
        x++; // 31
    }

	public static void main(String[] args) throws RunnerException {
		new Runner(new OptionsBuilder() //
				.include(MaxInlineSize.class.getName() + ".*") //
				.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining") //
				.jvmArgsAppend(
				        "-XX:TieredStopAtLevel=0", //
						"-XX:MaxInlineSize=35" //
                ) //
				.build()).run();
	}

}
