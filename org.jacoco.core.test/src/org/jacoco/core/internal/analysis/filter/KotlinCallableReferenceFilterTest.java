/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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

import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinCallableReferenceFilter}.
 *
 * TODO metadata kind synthetic class 3
 */
public class KotlinCallableReferenceFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinCallableReferenceFilter();

	/**
	 * <pre>
	 * import kotlin.reflect.KCallable
	 *
	 * fun example(): KCallable<*> =
	 *   ::example // line 4
	 * </pre>
	 */
	@Test
	public void should_filter_function_reference() {
		context.classAccess = Opcodes.ACC_SYNTHETIC;
		context.className = "ExampleKt$example$1";
		context.superClassName = "kotlin/jvm/internal/FunctionReferenceImpl";
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "invoke",
				"()Lkotlin/reflect/KCallable;",
				"()Lkotlin/reflect/KCallable<*>;", null);
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(4, label0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC,
				"org/jacoco/core/test/validation/kotlin/targets/KotlinCallableReferenceTargetKt",
				"example", "()Lkotlin/reflect/KCallable;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * TODO add validation test
	 * 
	 * <pre>
	 * import kotlin.reflect.KCallable
	 *
	 * class Example(var p: String) {
	 *   fun example(): KCallable<*> =
	 *     ::p // line 5
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_property_reference() {
		context.classAccess = Opcodes.ACC_SYNTHETIC;
		context.className = "Example$example$1";
		context.superClassName = "kotlin/jvm/internal/PropertyReference0Impl";
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "get",
				"()Ljava/lang/Object;", null, null);
		m.visitCode();
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(5, label0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitFieldInsn(Opcodes.GETFIELD, "Example$example$1", "receiver",
				"Ljava/lang/Object;");
		m.visitTypeInsn(Opcodes.CHECKCAST, "Example");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Example", "getP",
				"()Ljava/lang/String;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
