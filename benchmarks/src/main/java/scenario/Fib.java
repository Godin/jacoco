package scenario;

import org.jacoco.benchmark.InstrumentationBenchmark;

public class Fib implements InstrumentationBenchmark.ITarget {
	// TODO pass CompilerControl as JVM argument to have in a single run
	// both compiled and not
	// https://docs.oracle.com/en/java/javase/17/vm/commands-work-directive-files.html
	// @CompilerControl(CompilerControl.Mode.EXCLUDE)
	public long run() {
		long current = 1;
		long next = 1;
		for (int i = 2; i < 10; i++) {
			long temp = current + next;
			current = next;
			next = temp;
		}
		return next;
	}
}
