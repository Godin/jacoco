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
package org.jacoco.core.internal.instr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class InvokeDynamicProbeArrayStrategy implements IProbeArrayStrategy {

	static final String BOOTSTRAP_METHOD_NAME = "$jacocoBootstrap";
	static final String BOOTSTRAP_METHOD_DESC = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;JI)Ljava/lang/invoke/CallSite;";

	private final String systemClassName;
	private final String accessFieldName;

	private final String className;
	private final long classId;

	public InvokeDynamicProbeArrayStrategy(final String systemClassName,
			final String accessFieldName, final String className,
			final long classId) {
		this.systemClassName = systemClassName;
		this.accessFieldName = accessFieldName;
		this.className = className;
		this.classId = classId;
	}

	public int storeInstance(final MethodVisitor mv, final int variable) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				false);
		return 1;
	}

	public void addMembers(ClassVisitor cv, int probeCount) {
		createBootstrapMethod(cv);
		createInitMethod(cv, probeCount);
	}

	private void createInitMethod(final ClassVisitor cv, final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				null, null);
		mv.visitCode();
		mv.visitInvokeDynamicInsn("$jacocoGetProbeArray", "()[Z", new Handle(
				Opcodes.H_INVOKESTATIC, className, BOOTSTRAP_METHOD_NAME,
				BOOTSTRAP_METHOD_DESC, false), Long.valueOf(classId), Integer
				.valueOf(probeCount));
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(1, 0);
		mv.visitEnd();
	}

	private void createBootstrapMethod(final ClassVisitor cv) {
		final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				+ Opcodes.ACC_STATIC, BOOTSTRAP_METHOD_NAME,
				BOOTSTRAP_METHOD_DESC, null, null);
		mv.visitCode();

		mv.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
		mv.visitInsn(Opcodes.DUP);

		mv.visitLdcInsn(Type.getType(boolean[].class));

		// Get probe array
		mv.visitFieldInsn(Opcodes.GETSTATIC, systemClassName, accessFieldName, /* ACCESS_FIELD_TYPE */
				"Ljava/lang/Object;");

		mv.visitInsn(Opcodes.ICONST_3);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
		// classId
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitVarInsn(Opcodes.LLOAD, 3);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;", false);
		mv.visitInsn(Opcodes.AASTORE);
		// className
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.ACONST_NULL);
		mv.visitInsn(Opcodes.AASTORE);
		// probesCount
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_2);
		mv.visitVarInsn(Opcodes.ILOAD, 5);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
				"valueOf", "(I)Ljava/lang/Integer;", false);
		mv.visitInsn(Opcodes.AASTORE);

		mv.visitInsn(Opcodes.DUP_X1);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals",
				"(Ljava/lang/Object;)Z", false);
		mv.visitInsn(Opcodes.POP);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.AALOAD);
		mv.visitTypeInsn(Opcodes.CHECKCAST, InstrSupport.DATAFIELD_DESC);

		mv.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				"java/lang/invoke/MethodHandles",
				"constant",
				"(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;",
				false);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/invoke/ConstantCallSite", "<init>",
				"(Ljava/lang/invoke/MethodHandle;)V", false);

		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

}
