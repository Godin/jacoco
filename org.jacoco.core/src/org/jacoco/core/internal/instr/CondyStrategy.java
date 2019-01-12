/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This strategy for Java 11+ uses {@link ConstantDynamic} to hold the probe
 * array and adds bootstrap method requesting the probe array from the runtime.
 */
public class CondyStrategy implements IProbeArrayStrategy {

	private static final String B_DESC = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z";

	private final String className;

	private final long classId;

	private final boolean isInterface;

	private final IExecutionDataAccessorGenerator accessorGenerator;

	CondyStrategy(final String className, final long classId,
			final boolean isInterface,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.isInterface = isInterface;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		final Handle bootstrapMethod = new Handle(Opcodes.H_INVOKESTATIC,
				className, InstrSupport.INITMETHOD_NAME, B_DESC, isInterface);
		mv.visitLdcInsn(new ConstantDynamic("probes",
				InstrSupport.DATAFIELD_DESC, bootstrapMethod));
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}

	public void addMembers(final ClassVisitor cv, final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, B_DESC, null, null);

		final int maxStack = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(maxStack, /* args */ 3);

		mv.visitEnd();
	}

}
