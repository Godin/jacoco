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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Requires Java 12.
 */
public final class InjectedClassRuntime extends AbstractRuntime {

	private static final String FIELD_NAME = "$jacocoAccess";

	private static final String FIELD_TYPE = "Ljava/lang/Object;";

	private final String className;

	private final Field field;

	private InjectedClassRuntime(final String className, final Field field) {
		this.className = className;
		this.field = field;
	}

	/**
	 * Creates a new {@link InjectedClassRuntime}.
	 *
	 * @param target
	 *            the target class
	 * @return new runtime instance
	 *
	 * @throws RuntimeException
	 *             if can not perform injection
	 */
	public static InjectedClassRuntime create(final Class target) {
		// should be in the same package as target
		final String className = target.getName().replace('.', '/')
				+ FIELD_NAME;
		final byte[] classBytes = createClass(className);
		final Class cls;
		try {
			cls = inject(target, classBytes);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		try {
			return new InjectedClassRuntime(className,
					cls.getField(FIELD_NAME));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] createClass(final String className) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V11 + 1, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
				className, null, "java/lang/Object", null);
		cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FIELD_NAME,
				FIELD_TYPE, null, null);
		cw.visitEnd();
		return cw.toByteArray();
	}

	/**
	 * Defines given class bytes in the same class loader and in the same
	 * runtime package and protection domain as given target class.
	 *
	 * <pre>
	 * {@code
	 * MethodHandles.privateLookupIn(Object.class, MethodHandles.lookup()).defineClass(classBytes);
	 * }
	 * </pre>
	 *
	 * Requires Java 9.
	 *
	 * @param target
	 *            the target class
	 * @param classBytes
	 *            the class bytes
	 * @return injected class
	 */
	private static Class inject(final Class target, final byte[] classBytes)
			throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		final Class<?> classMethodHandles = Class
				.forName("java.lang.invoke.MethodHandles");
		final Class<?> classLookup = Class
				.forName("java.lang.invoke.MethodHandles$Lookup");

		final Object lookup = classMethodHandles.getMethod("lookup")
				.invoke(null);

		// throws IllegalArgumentException - if targetClass is a primitve type
		// or array class
		//
		// throws IllegalAccessException - if module of target class does not
		// open package containing target class to module of this class
		//
		// throws SecurityException - if denied by the security manager
		//
		final Object privateLookupInTarget = classMethodHandles
				.getMethod("privateLookupIn", Class.class, classLookup)
				.invoke(null, target, lookup);

		// throws IllegalArgumentException - the bytes are for a class in a
		// different package to the lookup class
		//
		// throws IllegalAccessException - if lookup does not have PACKAGE
		// access
		//
		// throws LinkageError - if the class is malformed
		// (ClassFormatError), cannot be verified (VerifyError), is already
		// defined, or another linkage error occurs
		//
		// throws SecurityException - if denied by the security manager
		//
		return (Class) classLookup.getMethod("defineClass", byte[].class)
				.invoke(privateLookupInTarget, (Object) classBytes);

	}

	@Override
	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		field.set(null, data);
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, FIELD_NAME, FIELD_TYPE);

		RuntimeData.generateAccessCall(classid, classname, probecount, mv);

		return 6;
	}

}
