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

public class RuntimeData3 extends RuntimeData {

    private ExecutionData[] store = new ExecutionData[32];

    private int nextSlot = 0;

    public void collect(final IExecutionDataVisitor executionDataVisitor,
                        final ISessionInfoVisitor sessionInfoVisitor, final boolean reset) {
        synchronized (this) {
            final SessionInfo info = new SessionInfo(sessionId, startTimeStamp,
                    System.currentTimeMillis());
            sessionInfoVisitor.visitSessionInfo(info);
            for (final ExecutionData executionData : store) {
                executionDataVisitor.visitClassExecution(executionData);
            }
            if (reset) {
                reset();
            }
        }
    }

    public final void reset() {
        synchronized (this) {
            for (final ExecutionData executionData : store) {
                executionData.reset();
            }
            startTimeStamp = System.currentTimeMillis();
        }
    }

    public int newSlot(final Long id, final String name, final int probecount) {
        synchronized (this) {
            final int slot = nextSlot;
            nextSlot++;
            ensureCapacity(nextSlot);
            final ExecutionData executionData = new ExecutionData(id, name,
                    probecount);
            store[slot] = executionData;
            return slot;
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > store.length) {
            final int newCapacity = store.length * 2;
            final ExecutionData[] newStore = new ExecutionData[newCapacity];
            System.arraycopy(store, 0, newStore, 0, store.length);
            store = newStore;
        }
    }

    public ExecutionData getExecutionData(final Long id, final String name,
                                          final int probecount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getProbes(Object[] args) {
        final int slot = ((Long) args[0]).intValue();
        args[0] = store[slot].getProbes();
    }

}
