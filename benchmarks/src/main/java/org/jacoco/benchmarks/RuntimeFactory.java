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
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.runtime.URLStreamHandlerRuntime;

public enum RuntimeFactory {
    ModifiedSystemClass {
        @Override
        public IRuntime create() {
            return new EmulatedModifiedSystemClassRuntime();
        }
    },
    URLStreamHandlerRuntime {
        @Override
        IRuntime create() {
            return new URLStreamHandlerRuntime();
        }
    },
    SystemPropertiesRuntime {
        @Override
        IRuntime create() {
            return new SystemPropertiesRuntime();
        }
    },
    LoggerRuntime {
        @Override
        IRuntime create() {
            return new LoggerRuntime();
        }
    };

    abstract IRuntime create();
}
