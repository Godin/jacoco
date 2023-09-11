package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.Jit;
import org.jacoco.core.test.TargetLoader;
import org.junit.Test;

public class JitInlineTest {

	private static final JitInlineTest target = new JitInlineTest();

	private static boolean[] probes = new boolean[1];

	/**
	 * TODO
	 * <ul>
	 * <li>below "-XX:MaxInlineSize" so eligible for immediate inlining?</li>
	 * <li>callee must be compiled</li>
	 * </ul>
	 */
	private void hit(final boolean[] probes, final int id) {
		if (probes[id]) {
			probes[id] = true;
		}
	}

	private static void warm() {
		for (int i = 0; i < 1000000; ++i) {
			target.hit(probes, 0);
		}
	}

	/**
	 * TODO can "-Xcomp" be used to simulate what happens when method containing
	 * "jacocoInit" gets compiled?
	 */
	public static void main(String[] args) {
		// warm();
		target.hit(probes, 0);
	}

	@Test
	public void test() throws Exception {
		final byte[] classBytes = TargetLoader
				.getClassDataAsBytes(JitInlineTest.class);
		System.out.println(
				Jit.run(classBytes, true, JitInlineTest.class.getName()));
	}

}
