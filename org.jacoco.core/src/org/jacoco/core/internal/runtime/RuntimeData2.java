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
package org.jacoco.core.internal.runtime;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.RuntimeData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RuntimeData2 extends RuntimeData {

	private final Map<Long, ExecutionData> store = new ConcurrentHashMap<Long, ExecutionData>();

	public void collect(final IExecutionDataVisitor executionDataVisitor,
			final ISessionInfoVisitor sessionInfoVisitor, final boolean reset) {
		synchronized (store) {
			final SessionInfo info = new SessionInfo(sessionId, startTimeStamp,
					System.currentTimeMillis());
			sessionInfoVisitor.visitSessionInfo(info);
			for (final ExecutionData executionData : store.values()) {
				executionDataVisitor.visitClassExecution(executionData);
			}
			if (reset) {
				reset();
			}
		}
	}

	public final void reset() {
		synchronized (store) {
			for (final ExecutionData executionData : store.values()) {
				executionData.reset();
			}
			startTimeStamp = System.currentTimeMillis();
		}
	}

	public ExecutionData getExecutionData(final Long id, final String name,
			final int probecount) {
		ExecutionData executionData = store.get(id);
		if (executionData == null) {
			executionData = new ExecutionData(id, name, probecount);
			store.put(id, executionData);
		}
		return executionData;
	}

	@Override
	public void getProbes(Object[] args) {
		final Long classid = (Long) args[0];
		args[0] = store.get(classid).getProbes();
	}

}
