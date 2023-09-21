/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test;

import java.io.IOException;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @deprecated
 */
@Deprecated
public final class Jit {

	private Jit() {
	}

	/**
	 * @return instrumented classBytes
	 */
	public static byte[] instrument(final byte[] classBytes)
			throws IOException {
		return new Instrumenter(new IExecutionDataAccessorGenerator() {
			public int generateDataAccessor(final long classId,
					final String className, final int probeCount,
					final MethodVisitor mv) {
				InstrSupport.push(mv, probeCount);
				mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
				return 1;
			}
		}).instrument(classBytes, null);
	}

}
