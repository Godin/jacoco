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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * TODO This strategy for Java 7 classes...
 */
class IndyClassFieldProbeArrayStrategy implements IProbeArrayStrategy {

	private static final String BOOTSTRAP_DESC = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";

	private final String className;
	private final long classId;
	private final IExecutionDataAccessorGenerator accessorGenerator;

	IndyClassFieldProbeArrayStrategy(final String className, final long classId,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.className = className;
		this.classId = classId;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		mv.visitInvokeDynamicInsn("$jacocoInit", "()[Z",
				new Handle(Opcodes.H_INVOKESTATIC, className,
						InstrSupport.INITMETHOD_NAME, BOOTSTRAP_DESC, false));
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}

	public void addMembers(ClassVisitor cv, int probeCount) {
		createDataField(cv);
		createInitMethod(cv, probeCount);
	}

	private void createDataField(final ClassVisitor cv) {
		cv.visitField(InstrSupport.DATAFIELD_ACC, InstrSupport.DATAFIELD_NAME,
				InstrSupport.DATAFIELD_DESC, null, null);
	}

	/**
	 * <pre>
	 * MethodHandle methodHandle = caller.findStaticGetter(caller.lookupClass(),
	 * 		InstrSupport.DATAFIELD_NAME, boolean[].class);
	 * return new ConstantCallSite(methodHandle);
	 * </pre>
	 */
	private void createInitMethod(final ClassVisitor cv, final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, BOOTSTRAP_DESC, null,
				new String[] { "java/lang/Exception" });
		mv.visitCode();

		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);

		// Stack[0]: [Z

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);

		final int size = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		// Stack[0]: [Z

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);

		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitLabel(alreadyInitialized);

		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"java/lang/invoke/MethodHandles$Lookup", "lookupClass",
				"()Ljava/lang/Class;", false);

		// Stack[0]: Ljava/lang/invoke/MethodHandles$Lookup;
		// Stack[1]: Ljava/lang/Class;

		mv.visitLdcInsn("$jacocoData");
		mv.visitLdcInsn(Type.getType(boolean[].class));

		// Stack[0]: Ljava/lang/invoke/MethodHandles$Lookup;
		// Stack[1]: Ljava/lang/Class;
		// Stack[2]: Ljava/lang/String;
		// Stack[3]: Ljava/lang/Class;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"java/lang/invoke/MethodHandles$Lookup", "findStaticGetter",
				"(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
				false);

		// Stack[0]: Ljava/lang/invoke/MethodHandle;

		mv.visitVarInsn(Opcodes.ASTORE, 3);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
		mv.visitInsn(Opcodes.DUP);

		// Stack[0]: Ljava/lang/invoke/ConstantCallSite;
		// Stack[1]: Ljava/lang/invoke/ConstantCallSite;

		mv.visitVarInsn(Opcodes.ALOAD, 3);

		// Stack[0]: Ljava/lang/invoke/ConstantCallSite;
		// Stack[1]: Ljava/lang/invoke/ConstantCallSite;
		// Stack[2]: Ljava/lang/invoke/MethodHandle;

		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/invoke/ConstantCallSite", "<init>",
				"(Ljava/lang/invoke/MethodHandle;)V", false);

		// Stack[0]: Ljava/lang/invoke/ConstantCallSite;

		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(Math.max(4, size), 4);
		mv.visitEnd();
	}

}
