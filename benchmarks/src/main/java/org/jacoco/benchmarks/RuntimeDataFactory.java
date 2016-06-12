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

import org.jacoco.core.internal.runtime.RuntimeData2;
import org.jacoco.core.internal.runtime.RuntimeData3;
import org.jacoco.core.internal.runtime.RuntimeData4;
import org.jacoco.core.runtime.RuntimeData;

public enum RuntimeDataFactory {

	MOCK {
		@Override
		RuntimeData create() {
			return new MockRuntimeData();
		}
	},
	CURRENT {
		@Override
		RuntimeData create() {
			final RuntimeData result = new RuntimeData();
			result.getExecutionData(DataAccessor.CLASS_ID,
					DataAccessor.CLASS_NAME, DataAccessor.PROBES_COUNT);
			return result;
		}
	},
	CONCURRENT_HASH_MAP {
		@Override
		RuntimeData create() {
			final RuntimeData2 result = new RuntimeData2();
            result.getExecutionData(DataAccessor.CLASS_ID,
                    DataAccessor.CLASS_NAME, DataAccessor.PROBES_COUNT);
			return result;
		}
	},
	ARRAY {
		@Override
		RuntimeData create() {
			final RuntimeData3 result = new RuntimeData3();
            result.newSlot(DataAccessor.CLASS_ID,
                    DataAccessor.CLASS_NAME, DataAccessor.PROBES_COUNT);
			return result;
		}
	},
	HASH_MAP {
		@Override
		RuntimeData create() {
			final RuntimeData4 result = new RuntimeData4();
			result.getExecutionData(DataAccessor.CLASS_ID,
					DataAccessor.CLASS_NAME, DataAccessor.PROBES_COUNT);
			return result;
		}
	};

	abstract RuntimeData create();

}
