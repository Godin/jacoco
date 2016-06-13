/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.benchmarks;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TargetLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class DG {

	private static final String CLASS_NAME = "DataAccessor";
	private static final String COMPANION_NAME = "Companion";

	/**
	 * Returns a reference to the probe array.
	 *
	 * @return the probe array
	 */
	public abstract boolean[] getData();

	private static TargetLoader loader = new TargetLoader();

	private static final java.lang.reflect.Method DEFINE_CLASS;
	static {
		try {
			DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass",
					String.class, byte[].class, Integer.TYPE, Integer.TYPE);
			DEFINE_CLASS.setAccessible(true);
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		} catch (final NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	public static boolean[] initializeCompanion() {
		System.out.println("Generating");
		Class<?> companion;
		companion = loader.get(COMPANION_NAME);
		if (companion != null) {
			try {
				return (boolean[]) companion.getField(InstrSupport.DATAFIELD_NAME).get(null);
			} catch (IllegalAccessException e) {
				throw new RuntimeException();
			} catch (NoSuchFieldException e) {
				throw new RuntimeException();
			}
		}
		final byte[] definition = createCompanionClass(COMPANION_NAME);
		final boolean[] probes = new boolean[2];
		probes[0] = true;
		try {
			companion = (Class<?>) DEFINE_CLASS.invoke(loader,
					COMPANION_NAME.replace('/', '.'), definition, 0,
					definition.length);
			companion.getField(InstrSupport.DATAFIELD_NAME).set(null, probes);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		return probes;
	}

	private static byte[] createCompanionClass(String companionName) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_1,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
				companionName, null, "java/lang/Object", null);
		cw.visitField(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null,
				null);
		cw.visitEnd();
		return cw.toByteArray();
	}

	public static void main(String[] args) throws InterruptedException {
		long time = System.currentTimeMillis();
		DG target = create();
		if (false)
			initializeCompanion();
		for (int i = 0; i < 8000; i++) {
			System.out.println(Arrays.toString(target.getData()));
		}
		System.out.println(Arrays.toString(target.getData()));
		time = System.currentTimeMillis() - time;
		System.out.println(time);
	}

	public static DG create() {
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, CLASS_NAME, null,
				Type.getInternalName(DG.class), new String[] {});

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(
				writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null,
						new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(DG.class),
				new Method("<init>", "()V"));
		gen.returnValue();
		gen.visitMaxs(1, 0);
		gen.visitEnd();

		// getData()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
				"getData", "()[Z", null, new String[0]), Opcodes.ACC_PUBLIC,
				"getData", "()[Z");

		gen.visitCode();

		Label start = gen.newLabel();
		gen.visitLabel(start);
		gen.getStatic(Type.getObjectType(COMPANION_NAME),
				InstrSupport.DATAFIELD_NAME, Type.getType("[Z"));
		Label end = gen.newLabel();
		gen.visitLabel(end);

		gen.returnValue();

		Label handler = gen.newLabel();
		gen.visitLabel(handler);

		gen.visitInsn(Opcodes.POP); // instance of exception
		gen.invokeStatic(Type.getType(DG.class),
				new Method("initializeCompanion", "()[Z"));
		gen.goTo(end);

		gen.visitTryCatchBlock(start, end, handler, /* any */ null);

		gen.visitMaxs(2, 0);
		gen.visitEnd();

		writer.visitEnd();

		try {
			return (DG) loader
					.add(CLASS_NAME.replace('/', '.'), writer.toByteArray())
					.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
