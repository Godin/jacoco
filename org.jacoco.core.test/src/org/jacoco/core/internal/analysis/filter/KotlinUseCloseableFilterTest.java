/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinUseCloseableFilter}.
 */
public class KotlinUseCloseableFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinUseCloseableFilter();

	@Test
	public void should_filter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"m", "()V", null, null);
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		final Label bodyStart = new Label();
		final Label bodyEnd = new Label();
		final Label catchThrowable = new Label();
		final Label catchAny = new Label();
		m.visitTryCatchBlock(bodyStart, bodyEnd, catchThrowable,
				"java/lang/Throwable");
		m.visitTryCatchBlock(bodyStart, bodyEnd, catchAny, null);
		m.visitTryCatchBlock(catchThrowable, catchAny, catchAny, null);

		m.visitTypeInsn(Opcodes.NEW, "java/io/ByteArrayOutputStream");
		m.visitInsn(Opcodes.DUP);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V", false);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java.io.Closeable");
		m.visitVarInsn(Opcodes.ASTORE, 1);

		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Throwable");
		m.visitVarInsn(Opcodes.ASTORE, 3);

		m.visitLabel(bodyStart);
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(bodyEnd);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/io/CloseableKt",
				"closeFinally", "(Ljava/io/Closeable;Ljava/lang/Throwable;)V",
				false);
		final Label exit = new Label();
		m.visitJumpInsn(Opcodes.GOTO, exit);

		m.visitLabel(catchThrowable);
		final Range range = new Range();
		range.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 4);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitInsn(Opcodes.ATHROW);

		m.visitLabel(catchAny);
		m.visitVarInsn(Opcodes.ASTORE, 4);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/io/CloseableKt",
				"closeFinally", "(Ljava/io/Closeable;Ljava/lang/Throwable;)V",
				false);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();

		m.visitLabel(exit);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
	}

}
