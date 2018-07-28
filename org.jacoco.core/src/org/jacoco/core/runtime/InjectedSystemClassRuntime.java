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
package org.jacoco.core.runtime;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * TODO
 */
public class InjectedSystemClassRuntime extends AbstractRuntime {

	private static final String FIELD_NAME = "$jacocoAccess";
	private static final String FIELD_TYPE = "Ljava/lang/Object;";

	private final String className;

	public static InjectedSystemClassRuntime create() {
		return create(Object.class, "java/lang/$JaCoCo");
	}

	public static InjectedSystemClassRuntime create(final Class target,
			final String className) {
		try {
			inject(target, createClass(className));
			return new InjectedSystemClassRuntime(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] createClass(final String className) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
				className, null, "java/lang/Object", null);
		cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FIELD_NAME,
				FIELD_TYPE, null, null);
		cw.visitEnd();
		return cw.toByteArray();
	}

	/**
	 * <pre>
	 * {@code
	 * MethodHandles.privateLookupIn(Object.class, MethodHandles.lookup()).defineClass(classBytes);
	 * }
	 * </pre>
	 *
	 * @throws IllegalAccessException
	 *             if module java.base does not open java.lang
	 */
	private static void inject(final Class target, final byte[] classBytes)
			throws ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException, IllegalAccessException {
		final Class<?> classMethodHandles = Class
				.forName("java.lang.invoke.MethodHandles");
		final Class<?> classLookup = Class
				.forName("java.lang.invoke.MethodHandles$Lookup");
		final Object lookup = classMethodHandles
				.getMethod("privateLookupIn", Class.class, classLookup)
				.invoke(null, target,
						classMethodHandles.getMethod("lookup").invoke(null));
		classLookup.getMethod("defineClass", byte[].class).invoke(lookup,
				(Object) classBytes);
	}

	private InjectedSystemClassRuntime(final String className) {
		super();
		this.className = className;
	}

	public void startup(RuntimeData data) throws Exception {
		super.startup(data);
		final Field field = Class.forName(className.replace('/', '.'))
				.getField(FIELD_NAME);
		field.set(null, data);
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(long classid, String classname,
			int probecount, MethodVisitor mv) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, FIELD_NAME, FIELD_TYPE);

		RuntimeData.generateAccessCall(classid, classname, probecount, mv);

		return 6;
	}

}
