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
package org.jacoco.core.internal.runtime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class InjectedClassRuntimeTest {

	@Test
	public void should_inject_into_bootstrap_classloader() throws Exception {
		final String name = "java.lang.JaCoCo";
		final byte[] classBytes = createClass(name);

		final Class<?> definedClass = InjectedClassRuntime
				.defineClass(classBytes, null);

		assertNotNull(Class.forName(name));
		assertSame(Class.forName(name), definedClass);
		assertNull("bootstrap classloader", definedClass.getClassLoader());
	}

	private byte[] createClass(final String name) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_5, 0, name.replace('.', '/'), null,
				"java/lang/Object", null);
		cw.visitEnd();
		return cw.toByteArray();
	}

}
