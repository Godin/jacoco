/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Unit test for {@link ScalaCaseClassFilter}.
 */
public class ScalaCaseClassFilterTest extends FilterTestBase {

	private final ScalaCaseClassFilter filter = new ScalaCaseClassFilter();

	@Test
	public void should_filter_equals() {
		context.classAnnotations.add("Lscala/reflect/ScalaSignature;");
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"equals", "(Ljava/lang/Object;)Z", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		Label L1 = new Label();
		m.visitJumpInsn(Opcodes.IF_ACMPEQ, L1);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitTypeInsn(Opcodes.INSTANCEOF, "CaseClass");
		Label L2 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, L2);
		m.visitInsn(Opcodes.ICONST_1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitJumpInsn(Opcodes.GOTO, new Label());
		m.visitLabel(L2);
		Label L4 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, L4);
		m.visitLabel(L4);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		Label L3 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, L3);
		m.visitLabel(L3);

		m.visitVarInsn(Opcodes.ILOAD, 2);
		Label L5 = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, L5);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitTypeInsn(Opcodes.CHECKCAST, "CaseClass");
		m.visitVarInsn(Opcodes.ASTORE, 4);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "CaseClass", "i", "()I",
				false);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "CaseClass", "i", "()I",
				false);
		Label L8 = new Label();
		m.visitJumpInsn(Opcodes.IF_ICMPNE, L8);

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "CaseClass", "s",
				"()Ljava/lang/String;", false);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "CaseClass", "s",
				"()Ljava/lang/String;", false);
		m.visitVarInsn(Opcodes.ASTORE, 5);
		m.visitInsn(Opcodes.DUP);
		Label L6 = new Label();
		m.visitJumpInsn(Opcodes.IFNONNULL, L6);
		m.visitInsn(Opcodes.POP);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		Label L7 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, L7);
		m.visitJumpInsn(Opcodes.GOTO, L8);
		m.visitLabel(L6);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, L8);

		m.visitLabel(L7);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "CaseClass", "canEqual",
				"(Ljava/lang/Object;)Z", false);
		m.visitJumpInsn(Opcodes.IFEQ, L8);
		m.visitInsn(Opcodes.ICONST_1);
		Label L9 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, L9);

		m.visitLabel(L8);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitLabel(L9);
		m.visitJumpInsn(Opcodes.IFEQ, L5);
		m.visitLabel(L1);
		m.visitInsn(Opcodes.ICONST_1);
		Label L10 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, L10);
		m.visitLabel(L5);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitLabel(L10);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_productPrefix() {
		context.className = "CaseClass";
		context.classAnnotations.add("Lscala/reflect/ScalaSignature;");
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productPrefix", "()Ljava/lang/String;", null, null);
		m.visitLdcInsn(context.className);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_productIterator() {
		context.classAnnotations.add("Lscala/reflect/ScalaSignature;");
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productIterator", "()Lscala/collection/Iterator;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/ScalaRunTime$",
				"MODULE$", "Lscala/runtime/ScalaRunTime$");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
				"typedProductIterator",
				"(Lscala/Product;)Lscala/collection/Iterator;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_hashCode() {
		context.classAnnotations.add("Lscala/reflect/ScalaSignature;");
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/ScalaRunTime$",
				"MODULE$", "Lscala/runtime/ScalaRunTime$");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
				"_hashCode", "(Lscala/Product;)I", false);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_toString() {
		context.classAnnotations.add("Lscala/reflect/ScalaSignature;");
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()Ljava/lang/String;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/ScalaRunTime$",
				"MODULE$", "Lscala/runtime/ScalaRunTime$");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
				"_toString", "(Lscala/Product;)Ljava/lang/String;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
