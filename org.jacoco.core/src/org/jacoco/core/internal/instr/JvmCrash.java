/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * javac -cp asm-5.2.jar -source 7 -target 7 JvmCrash.java
 * java -cp asm-5.2.jar:. JvmCrash
 */
public class JvmCrash {

	public static void main(final String[] args) throws Exception {
		System.out.println("Using getstatic directly");
		final Class direct = create(false);
		for (int i = 0; i < 100_000; i++) {
			direct.newInstance();
		}

		System.out.println("Using getstatic via invokedynamic");
		final Class indy = create(true);
		// needs warmup
		for (int i = 0; i < 10_000; i++) {
			indy.newInstance();
		}
	}

	public static boolean[] data = new boolean[1];

	/**
	 * Create class whose constructor is
	 * 
	 * <pre>
	 *     invokedynamic / getstatic
	 *     iconst_0
	 *     iconst_1
	 *     bastore
	 *
	 *     aload_0
	 *     invokespecial
	 *     return
	 * </pre>
	 */
	private static Class create(final boolean indy) throws Exception {
		final String className = "Example";
		final String mainName = JvmCrash.class.getName().replace('.', '/');

		final ClassWriter cw = new ClassWriter(
				ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
				className, null, "java/lang/Object", null);

		final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
				"()V", null, null);
		mv.visitCode();

		if (indy) {
			mv.visitInvokeDynamicInsn("bootstrap", "()[Z",
					new Handle(Opcodes.H_INVOKESTATIC, mainName, "bootstrap",
							"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
							false));
		} else {
			mv.visitFieldInsn(Opcodes.GETSTATIC, mainName, "data", "[Z");
		}

		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.BASTORE);

		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		mv.visitInsn(Opcodes.RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();

		cw.visitEnd();

		final byte[] bytes = cw.toByteArray();

		return new ClassLoader() {
			Class load() {
				return defineClass(className, bytes, 0, bytes.length);
			}
		}.load();
	}

	public static CallSite bootstrap(final MethodHandles.Lookup caller,
			final String name, final MethodType type) throws Exception {
		return new ConstantCallSite(
				caller.findStaticGetter(JvmCrash.class, "data", boolean[].class));
	}

}
