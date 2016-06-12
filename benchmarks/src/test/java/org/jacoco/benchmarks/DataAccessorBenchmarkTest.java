/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.benchmarks;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DataAccessorBenchmarkTest {

	@Test
	public void test() throws Exception {
		final DataAccessorBenchmark benchmark = new DataAccessorBenchmark();
		benchmark.runtime = RuntimeFactory.ModifiedSystemClass;
		benchmark.runtimeData = RuntimeDataFactory.MOCK;
		benchmark.setup();
		assertNotNull(benchmark.getProbes());
	}

}
