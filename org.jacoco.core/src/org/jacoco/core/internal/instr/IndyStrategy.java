/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * For >= Java 9, because of bugs in Java 8.
 */
class IndyStrategy implements IProbeArrayStrategy {

	private final String className;
	private final long classId;
	private final IExecutionDataAccessorGenerator accessorGenerator;

	IndyStrategy(final String className, final long classId,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(MethodVisitor mv, boolean clinit, int variable) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				false);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}

	public void addMembers(ClassVisitor cv, int probeCount) {
		createInitMethod(cv);
		createBootstrapMethod(cv, probeCount);
	}

	/**
	 * <pre>
	 *   boolean[] $jacocoInit() {
	 *     invokedynamic
	 *   }
	 * </pre>
	 */
	private void createInitMethod(final ClassVisitor cv) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				null, null);
		mv.visitCode();

		Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC, className, B_NAME,
				B_DESC, false);
		mv.visitInvokeDynamicInsn("getProbes",
				/* InstrSupport.INITMETHOD_DESC */"()[Z", bootstrap);
		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(1, 0);

		mv.visitEnd();
	}

	private static final String B_NAME = "$jacocoBoot";
	private static final String B_DESC = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";

	/**
	 * <pre>
	 *   java.lang.invoke.CallSite bootstrap(java.lang.invoke.MethodHandles.Lookup, java.lang.String, java.lang.invoke.MethodType) {
	 *     boolean[] probes = ...;
	 *     return new java.lang.invoke.ConstantCallSite(java.lang.invoke.MethodHandles.constant(boolean[].class, probes));
	 *   }
	 * </pre>
	 */
	private void createBootstrapMethod(final ClassVisitor cv,
			final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				B_NAME, B_DESC, null, null);
		mv.visitCode();

		final int size = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		mv.visitVarInsn(Opcodes.ASTORE, 1);

		mv.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(Type.getType(boolean[].class));
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"java/lang/invoke/MethodHandles", "constant",
				"(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;",
				false);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/invoke/ConstantCallSite", "<init>",
				"(Ljava/lang/invoke/MethodHandle;)V", false);
		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(size, 1 + /* args */ 3);

		mv.visitEnd();
	}

}
