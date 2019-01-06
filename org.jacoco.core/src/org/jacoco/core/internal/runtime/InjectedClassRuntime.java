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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectedClassRuntime extends AbstractRuntime {

	private static final String ACCESS_FIELD_NAME = "access";

	private static final String ACCESS_FIELD_TYPE = "Ljava/lang/Object;";

	private final Class<?> cls;

	public InjectedClassRuntime() throws Exception {
		this.cls = defineClass(
				createClass("java.lang.$JaCoCo" + System.currentTimeMillis()),
				null);
	}

	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);

		final Field field = cls.getField(ACCESS_FIELD_NAME);
		field.set(null, data);
	}

	public void shutdown() {
		// nothing to do
	}

	private static byte[] createClass(final String name) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC,
				name.replace('.', '/'), null, "java/lang/Object", null);

		cw.visitField(
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT
						| Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
				ACCESS_FIELD_NAME, ACCESS_FIELD_TYPE, null, null);

		cw.visitEnd();
		return cw.toByteArray();
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {

		mv.visitFieldInsn(Opcodes.GETSTATIC, cls.getName().replace('.', '/'),
				ACCESS_FIELD_NAME, ACCESS_FIELD_TYPE);

		RuntimeData.generateAccessCall(classid, classname, probecount, mv);

		return 6;
	}

	static Class<?> defineClass(final byte[] classBytes,
			final ClassLoader classLoader) throws ClassNotFoundException,
			NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		final Class<?> unsafeType = Class.forName("sun.misc.Unsafe");

		final Field theUnsafeField = unsafeType.getDeclaredField("theUnsafe");
		theUnsafeField.setAccessible(true);

		final Object unsafe = theUnsafeField.get(null);

		try {
			final Method defineClassMethod = unsafeType.getMethod("defineClass",
					String.class, byte[].class, int.class, int.class,
					ClassLoader.class, ProtectionDomain.class);

			return (Class<?>) defineClassMethod.invoke(unsafe, null, classBytes,
					0, classBytes.length, classLoader, null);

		} catch (NoSuchMethodException e) {
			// JDK >= 11

			final Method putBooleanMethod = unsafeType.getMethod("putBoolean",
					Object.class, long.class, boolean.class);

			final Method objectFieldOffsetMethod = unsafeType
					.getMethod("objectFieldOffset", Field.class);

			// FIXME NoSuchFieldException on JDK 12+
			// https://github.com/raphw/byte-buddy/blob/b1d6a0fb9884e433d8ebc2dd29747c9d34b6de21/byte-buddy-dep/src/main/java/net/bytebuddy/dynamic/loading/ClassInjector.java#L1035
			final long accessibleObjectOverrideFieldOffset = (Long) objectFieldOffsetMethod
					.invoke(unsafe, AccessibleObject.class
							.getDeclaredField("override"));

			final Class<?> unsafe9Type = Class
					.forName("jdk.internal.misc.Unsafe");

			final Field theUnsafe9Field = unsafe9Type
					.getDeclaredField("theUnsafe");
			putBooleanMethod.invoke(unsafe, theUnsafe9Field,
					accessibleObjectOverrideFieldOffset, true);

			final Object theUnsafe9 = theUnsafe9Field.get(null);

			final Method defineClassMethod = unsafe9Type.getMethod(
					"defineClass", String.class, byte[].class, int.class,
					int.class, ClassLoader.class, ProtectionDomain.class);
			putBooleanMethod.invoke(unsafe, defineClassMethod,
					accessibleObjectOverrideFieldOffset, true);

			return (Class<?>) defineClassMethod.invoke(theUnsafe9, null,
					classBytes, 0, classBytes.length, classLoader, null);
		}
	}

}
