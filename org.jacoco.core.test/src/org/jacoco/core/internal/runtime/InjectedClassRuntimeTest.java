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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.test.TargetLoader;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

/**
 * Unit tests for {@link InjectedClassRuntime}.
 */
public class InjectedClassRuntimeTest {

	private final Class target = createTarget();

	@Test
	public void test() throws Exception {
		ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V11, 0, "java/lang/Foo", null, "java/lang/Object",
				null);
		cw.visitEnd();
		byte[] b = cw.toByteArray();

		defineClass("java/lang/Foo", b, 0, b.length, null, null);

		System.out.println(Class.forName("java.lang.Foo"));
	}

	private Class<?> defineClass(String name, byte[] b, int off, int len,
			ClassLoader classLoader, ProtectionDomain protectionDomain)
			throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, ClassNotFoundException,
			NoSuchFieldException {

		Class<?> unsafeType = Class.forName("sun.misc.Unsafe");

		Field theUnsafe = unsafeType.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);

		Method putBoolean = unsafeType.getMethod("putBoolean", Object.class,
				long.class, boolean.class);
		long offset = (Long) unsafeType
				.getMethod("objectFieldOffset", Field.class).invoke(unsafe,
						AccessibleObject.class.getDeclaredField("override"));

		Class unsafe12Type = Class.forName("jdk.internal.misc.Unsafe");
		Method getUnsafe = unsafe12Type.getMethod("getUnsafe");
		// getUnsafe.setAccessible(true)
		putBoolean.invoke(unsafe, getUnsafe, offset, true);
		Object unsafe12 = getUnsafe.invoke(null);

		Method defineCLass = unsafe12Type.getMethod("defineClass", String.class,
				byte[].class, int.class, int.class, ClassLoader.class,
				ProtectionDomain.class);
		// defineCLass.setAccessible(true);
		putBoolean.invoke(unsafe, defineCLass, offset, true);

		return (Class<?>) defineCLass.invoke(unsafe12, name, b, off, len,
				classLoader, protectionDomain);
	}

	@Test
	public void should_inject_into_bootstrap_classloader() throws Exception {
		final InjectedClassRuntime runtime = InjectedClassRuntime
				.create(Void.class);

		final Class injected = Class.forName("java.lang.Void$jacocoAccess");
		assertNull(injected.getClassLoader());
		assertTrue(injected.isSynthetic());

		final Field field = injected.getField("$jacocoAccess");
		assertTrue(Modifier.isPublic(field.getModifiers()));
		assertTrue(Modifier.isStatic(field.getModifiers()));
		assertEquals(Object.class, field.getType());

		final RuntimeData runtimeData = new RuntimeData();
		runtime.startup(runtimeData);
		assertSame(runtimeData, field.get(null));
	}

	@Test
	public void should_inject_into_custom_classloader() throws Exception {
		final InjectedClassRuntime runtime = InjectedClassRuntime
				.create(target);

		final Class injected = target.getClassLoader()
				.loadClass(target.getName() + "$jacocoAccess");
		assertSame(target.getClassLoader(), injected.getClassLoader());
		assertTrue(injected.isSynthetic());

		final Field field = injected.getField("$jacocoAccess");
		assertTrue(Modifier.isPublic(field.getModifiers()));
		assertTrue(Modifier.isStatic(field.getModifiers()));
		assertEquals(Object.class, field.getType());

		final RuntimeData runtimeData = new RuntimeData();
		runtime.startup(runtimeData);
		assertSame(runtimeData, field.get(null));
	}

	@Test
	public void should_not_inject_duplicate() {
		InjectedClassRuntime.create(target);

		try {
			InjectedClassRuntime.create(target);
			fail("exception expected");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage()
					.contains("attempted duplicate class definition"));
		}
	}

	private static Class createTarget() {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V11, 0, "foo/bar/Target", null, "java/lang/Object",
				null);
		cw.visitEnd();
		final byte[] classBytes = cw.toByteArray();

		try {
			return new TargetLoader().add("foo.bar.Target", classBytes);
		} catch (UnsupportedClassVersionError e) {
			// skip tests
			throw new AssumptionViolatedException("can not load Java 12 class");
		}
	}

}
