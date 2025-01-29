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
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link KotlinMultifileFacadeFilter}.
 */
public class KotlinMultifileFacadeFilterTest extends FilterTestBase {

	private final KotlinMultifileFacadeFilter filter = new KotlinMultifileFacadeFilter();

	/**
	 * <pre>
	 * &#64;file:JvmName("Example")
	 * &#64;file:JvmMultifileClass
	 *
	 * fun example() {}
	 * </pre>
	 */
	@Test
	public void should_filter() {
		final AnnotationNode kotlinMetadata = new AnnotationNode(
				KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		kotlinMetadata.visit("k", 4);
		context.classAnnotationNodes.add(kotlinMetadata);
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC,
				"example", "()V", null, null);
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(1, label0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Example__ExampleKt", "example",
				"()V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
