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

import org.objectweb.asm.Opcodes;
import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link EnumStaticInitFilter}.
 */
public class EnumStaticInitFilterTest extends FilterTestBase {

	private final IFilter filter = new EnumStaticInitFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
			Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);

	@Test
	public void should_filter_javac() {
		context.className = "Target";
		context.superClassName = "java/lang/Enum";

		Label label = new Label();
		m.visitLabel(label);
		m.visitLineNumber(42, label);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitTypeInsn(Opcodes.ANEWARRAY, "Target");
		m.visitFieldInsn(Opcodes.PUTSTATIC, "Target", "$VALUES", "[LE");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_filter_ecj() {
		context.className = "Target";
		context.superClassName = "java/lang/Enum";

		Label label = new Label();
		m.visitLabel(label);
		m.visitLineNumber(42, label);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitTypeInsn(Opcodes.ANEWARRAY, "Target");
		m.visitFieldInsn(Opcodes.PUTSTATIC, "Target", "ENUM$VALUES", "[LE");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * enum Target {
	 * 	 C;
	 *   static {
	 *     TODO add some code
	 *   }
	 * }
	 * </pre>
	 */
	@Test
	public void should_not_filter() {
		context.className = "Target";
		context.superClassName = "java/lang/Enum";

		m.visitTypeInsn(Opcodes.NEW, "Target");
		m.visitInsn(Opcodes.DUP);
		m.visitLdcInsn("C");
		m.visitInsn(Opcodes.ICONST_0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Target", "<init>",
				"(Ljava/lang/String;I)V", false);
		m.visitFieldInsn(Opcodes.PUTSTATIC, "Target", "C", "LTarget");

		m.visitInsn(Opcodes.ICONST_1);
		m.visitTypeInsn(Opcodes.ANEWARRAY, "Target");
		m.visitInsn(Opcodes.DUP);
		m.visitInsn(Opcodes.ICONST_0);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Target", "C", "LTarget");
		m.visitInsn(Opcodes.AASTORE);
		m.visitFieldInsn(Opcodes.PUTSTATIC, "Target", "$VALUES", "[LE");
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
