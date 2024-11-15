/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/

/**
 *
 */
module org.jacoco.core {
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.commons;
    requires java.logging;
    requires java.instrument;

    exports org.jacoco.core;
    exports org.jacoco.core.analysis;
    exports org.jacoco.core.data;
    exports org.jacoco.core.instr;
    exports org.jacoco.core.runtime;
    exports org.jacoco.core.tools;
}
