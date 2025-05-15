package org.jacoco.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SanityBenchmark {

    public Object object;

    @Benchmark
    public Object obj_return() {
        return object;
    }

    @Benchmark
    public void obj_blackhole_consume(Blackhole blackhole) {
        blackhole.consume(object);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder() //
                .forks(1) //
                .warmupIterations(3) //
                .measurementIterations(3) //
                .mode(Mode.AverageTime) //
                // .addProfiler(XCTraceNormProfiler.class)
                .include(SanityBenchmark.class.getName()) //
                .build()).run();
    }

}
