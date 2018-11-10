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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NewStrategy implements IProbeArrayStrategy {

	private static final Object[] FRAME_STACK_ARRZ = new Object[] {
			InstrSupport.DATAFIELD_DESC };

	final boolean isInterface;
	final String className;
	private final long classId;
	private final int probeCount;
	private final boolean needsFrames;
	private final IExecutionDataAccessorGenerator accessorGenerator;

	private boolean seenClinit = false;

	public NewStrategy(final boolean isInterface, final String className,
			final long classId, final int probeCount, final boolean needsFrames,
			final IExecutionDataAccessorGenerator accessorGenerator) {
		this.isInterface = isInterface;
		this.className = className;
		this.classId = classId;
		this.probeCount = probeCount;
		this.needsFrames = needsFrames;
		this.accessorGenerator = accessorGenerator;
	}

	public int storeInstance(final MethodVisitor mv, final boolean clinit,
			final int variable) {
		int maxStack = 0;
		if (clinit) {
			seenClinit = true;
			maxStack = accessorGenerator.generateDataAccessor(classId,
					className, probeCount, mv);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
					InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
		}

		// FIXME ProbeInserter expects addition of variable
		mv.visitInsn(Opcodes.ACONST_NULL);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return Math.max(maxStack, 1);
	}

	public void addMembers(ClassVisitor cv, int probeCount) {
		createDataField(cv);
		if (!seenClinit) {
			createClinitMethod(cv, probeCount);
		}
		createHitMethod(cv);
	}

	private void createDataField(final ClassVisitor cv) {
		cv.visitField(InstrSupport.DATAFIELD_INTF_ACC,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null,
				null);
	}

	private void createClinitMethod(final ClassVisitor cv,
			final int probeCount) {
		final MethodVisitor mv = cv.visitMethod(InstrSupport.CLINIT_ACC,
				InstrSupport.CLINIT_NAME, InstrSupport.CLINIT_DESC, null, null);
		mv.visitCode();

		final int maxStack = accessorGenerator.generateDataAccessor(classId,
				className, probeCount, mv);

		// Stack[0]: [Z

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);

		mv.visitInsn(Opcodes.RETURN);

		mv.visitMaxs(maxStack, 0);
		mv.visitEnd();
	}

	private void createHitMethod(final ClassVisitor cv) {
		final MethodVisitor mv = cv
				.visitMethod(
						Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE
								| Opcodes.ACC_STATIC,
						"$jacocoHit", "(I)V", null, null);
		mv.visitCode();

		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC); // +3

		// ALOAD_0 // +1

		// if ($jacocoData == null) return
		mv.visitInsn(Opcodes.DUP); // +1
		final Label label1 = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, label1); // +3
		if (needsFrames) {
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, FRAME_STACK_ARRZ);
		}
		mv.visitInsn(Opcodes.RETURN); // +1
		mv.visitLabel(label1);

		// if ($jacocoData[id]) return
		mv.visitInsn(Opcodes.DUP);
		mv.visitVarInsn(Opcodes.ILOAD, 0);
		mv.visitInsn(Opcodes.BALOAD);
		final Label label2 = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, label2);
		if (needsFrames) {
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, FRAME_STACK_ARRZ);
		}
		mv.visitInsn(Opcodes.RETURN);
		mv.visitLabel(label2);

		// $jacocoData[id] = true
		mv.visitVarInsn(Opcodes.ILOAD, 0); // +1
		mv.visitInsn(Opcodes.ICONST_1); // +1
		mv.visitInsn(Opcodes.BASTORE); // +1
		mv.visitInsn(Opcodes.RETURN); // +1

		mv.visitMaxs(3, 1);
		mv.visitEnd();
	}

}
