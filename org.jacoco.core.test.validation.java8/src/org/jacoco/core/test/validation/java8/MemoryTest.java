package org.jacoco.core.test.validation.java8;

import org.jacoco.core.internal.analysis.LineImpl;
import org.junit.Test;
import org.openjdk.jol.datamodel.Model32;
import org.openjdk.jol.datamodel.Model64;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.layouters.HotSpotLayouter;
import org.openjdk.jol.layouters.Layouter;

public class MemoryTest {

	private static final Layouter layouter32 = new HotSpotLayouter(
			new Model32(), 8);

	private static final Layouter layouter64_coops = new HotSpotLayouter(
			new Model64(true, true), 8);

	private static final Layouter layouter64 = new HotSpotLayouter(
			new Model64(false, false), 8);

	@Test
	public void test() {
		System.out.println(ClassLayout.parseClass(LineImpl.class, layouter64_coops)
				.toPrintable());
		System.out.println(ClassLayout.parseClass(LineImpl.class, layouter64)
				.toPrintable());
		System.out.println(ClassLayout.parseClass(LineImpl.class, layouter32)
				.toPrintable());
	}

}
