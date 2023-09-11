package org.jacoco.core.test.validation.groovy;

import java.io.IOException;

import org.jacoco.core.internal.flow.AsmUtils;
import org.jacoco.core.test.Jit;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.groovy.targets.GroovySynchronizedTarget;
import org.junit.Assert;
import org.junit.Test;

public class GroovyJitTest {

	@Test
	public void print() throws IOException {
		System.out.println(AsmUtils.instrumentToString( //
				AsmUtils.classBytesToClassNode( //
						TargetLoader.getClassDataAsBytes( //
								GroovySynchronizedTarget.class //
						))));
	}

	@Test
	public void testGroovy() throws Exception {
		final byte[] classBytes = TargetLoader
				.getClassDataAsBytes(GroovySynchronizedTarget.class);
		Assert.assertEquals("", Jit.run(classBytes, false,
				GroovySynchronizedTarget.class.getName()));
		Assert.assertEquals("", Jit.run(Jit.instrument(classBytes), false,
				GroovySynchronizedTarget.class.getName()));
	}

}
