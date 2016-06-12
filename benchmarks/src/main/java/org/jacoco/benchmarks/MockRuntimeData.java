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

import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Allows to compare cost of infrastructure created by various
 * {@link IRuntime} around call of {@link RuntimeData#getProbes(Object[])}.
 */
public class MockRuntimeData extends RuntimeData {

    private static final boolean[] PROBES = new boolean[2];

    @Override
    public void getProbes(Object[] args) {
        args[0] = PROBES;
    }

}
