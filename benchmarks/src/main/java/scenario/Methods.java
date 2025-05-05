package scenario;

import org.jacoco.benchmark.InstrumentationBenchmark;

public class Methods implements InstrumentationBenchmark.ITarget {

	private long x;

	public long run() {
		return i1() + i1();
	}

	public long i1() {
		return i2() + i2();
	}

	public long i2() {
		return x++;
	}

}
