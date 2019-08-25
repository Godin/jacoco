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
 * Unit test for {@link ScalaLazyInitializerFilter}.
 */
public class ScalaLazyInitializerFilterTest extends FilterTestBase {

	private final IFilter filter = new ScalaLazyInitializerFilter();

	@Test
	public void should_filter_initializer_when_field_has_type_boolean() {
		final MethodNode m = createInitializer(1);
		context.classAttributes.add("ScalaSig");
		filter.filter(m, context, output);
		assertIgnored(new Range(m.instructions.get(6), m.instructions.get(6)),
				new Range(m.instructions.get(13), m.instructions.get(23)));
	}

	@Test
	public void should_filter_initializer_when_field_has_type_byte() {
		final MethodNode m = createInitializer(8);
		context.classAttributes.add("ScalaSig");
		filter.filter(m, context, output);
		assertIgnored(new Range(m.instructions.get(10), m.instructions.get(10)),
				new Range(m.instructions.get(21), m.instructions.get(31)));
	}

	@Test
	public void should_filter_initializer_when_field_has_type_int() {
		final MethodNode m = createInitializer(32);
		context.classAttributes.add("ScalaSig");
		filter.filter(m, context, output);
		assertIgnored(new Range(m.instructions.get(9), m.instructions.get(9)),
				new Range(m.instructions.get(19), m.instructions.get(29)));
	}

	@Test
	public void should_filter_initializer_when_field_has_type_long() {
		final MethodNode m = createInitializer(64);
		context.classAttributes.add("ScalaSig");
		filter.filter(m, context, output);
		assertIgnored(new Range(m.instructions.get(10), m.instructions.get(10)),
				new Range(m.instructions.get(20), m.instructions.get(30)));
	}

	@Test
	public void should_filter_getter() {
		final MethodNode m = createGetter();
		context.classAttributes.add("ScalaSig");
		filter.filter(m, context, output);
		assertIgnored(
				new Range(m.instructions.getFirst(), m.instructions.getLast()));
	}

	private static MethodNode createInitializer(final int bits) {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"field$lzycompute", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		final String bitmapFieldDescriptor;
		if (bits == 1) {
			bitmapFieldDescriptor = "Z";
		} else if (bits <= 8) {
			bitmapFieldDescriptor = "B";
		} else if (bits <= 32) {
			bitmapFieldDescriptor = "I";
		} else {
			bitmapFieldDescriptor = "J";
		}
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "bitmap$0",
				bitmapFieldDescriptor);
		final Label label1 = new Label();
		create(m, bits, label1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.NOP); // initializer
		m.visitFieldInsn(org.objectweb.asm.Opcodes.PUTFIELD, "Example", "field",
				"Ljava/lang/String;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		if (bits == 1) {
			m.visitInsn(Opcodes.ICONST_1);
		} else if (bits <= 8) {
			m.visitVarInsn(Opcodes.ALOAD, 0);
			m.visitFieldInsn(Opcodes.GETFIELD, "Example", "bitmap$0",
					bitmapFieldDescriptor);
			m.visitInsn(Opcodes.ICONST_1);
			m.visitInsn(Opcodes.IOR);
			m.visitInsn(Opcodes.I2B);
		} else if (bits <= 32) {
			m.visitVarInsn(Opcodes.ALOAD, 0);
			m.visitFieldInsn(Opcodes.GETFIELD, "Example", "bitmap$0",
					bitmapFieldDescriptor);
			m.visitInsn(Opcodes.ICONST_1);
			m.visitInsn(Opcodes.IOR);
		} else {
			m.visitVarInsn(Opcodes.ALOAD, 0);
			m.visitFieldInsn(Opcodes.GETFIELD, "Example", "bitmap$0",
					bitmapFieldDescriptor);
			m.visitInsn(Opcodes.LCONST_1);
			m.visitInsn(Opcodes.LOR);
		}
		m.visitFieldInsn(Opcodes.PUTFIELD, "Example", "bitmap$0",
				bitmapFieldDescriptor);

		m.visitLabel(label1);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitInsn(Opcodes.ATHROW);
		m.visitVarInsn(Opcodes.ALOAD, 0);

		m.visitLabel(label2);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "field",
				"Ljava/lang/String;");
		m.visitInsn(Opcodes.ARETURN);
		return m;
	}

	private static void create(final MethodNode m, final int bits,
			final Label label) {
		if (bits == 1) {
			m.visitJumpInsn(Opcodes.IFNE, label);
		} else if (bits <= 8) {
			m.visitInsn(Opcodes.ICONST_1);
			m.visitInsn(Opcodes.IAND);
			m.visitInsn(Opcodes.I2B);
			m.visitInsn(Opcodes.ICONST_0);
			m.visitJumpInsn(Opcodes.IF_ICMPNE, label);
		} else if (bits <= 32) {
			m.visitInsn(Opcodes.ICONST_1);
			m.visitInsn(Opcodes.IAND);
			m.visitInsn(Opcodes.ICONST_0);
			m.visitJumpInsn(Opcodes.IF_ICMPNE, label);
		} else {
			m.visitInsn(Opcodes.LCONST_1);
			m.visitInsn(Opcodes.LAND);
			m.visitInsn(Opcodes.LCONST_0);
			m.visitInsn(Opcodes.LCMP);
			m.visitJumpInsn(Opcodes.IFNE, label);
		}
	}

	private static MethodNode createGetter() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"field", "()Ljava/lang/String;", null, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "bitmap$0", "Z");
		final Label label1 = new Label();
		create(m, 1, label1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Example", "field$lzycompute",
				"Ljava/lang/String;", false);
		final Label label2 = new Label();
		m.visitJumpInsn(Opcodes.GOTO, label2);
		m.visitLabel(label1);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example", "field",
				"Ljava/lang/String;");
		m.visitLabel(label2);
		m.visitInsn(Opcodes.ARETURN);
		return m;
	}

}
