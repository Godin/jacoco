/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.runtime;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

/**
 * {@link IRuntime} which defines a new class using
 * {@code java.lang.invoke.MethodHandles.Lookup.defineClass} introduced in Java
 * 9. Module where class will be defined must be opened to at least module of
 * this class.
 */
public class InjectedClassRuntime extends AbstractRuntime {

	private static final String FIELD_NAME = "data";

	private static final String FIELD_TYPE = "Ljava/lang/Object;";

	private final Class<?> locator;

	private final String injectedClassName;

	private Class<?> injected;

	/**
	 * Creates a new runtime which will define a class to the same class loader
	 * and in the same package and protection domain as given class.
	 *
	 * @param locator
	 *            class to identify the target class loader and package
	 * @param simpleClassName
	 *            simple name of the class to be defined
	 */
	public InjectedClassRuntime(final Class<?> locator,
			final String simpleClassName) {
		this.locator = locator;
		this.injectedClassName = locator.getPackage().getName().replace('.',
				'/') + '/' + simpleClassName;
	}

	public static void patch(final Instrumentation instrumentation) throws UnmodifiableClassException {
		final ClassFileTransformer retransformer = new ClassFileTransformer() {
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes) {
				if (!"java/lang/Class".equals(className)) {
					return null;
				}
				ClassNode classNode = new ClassNode();
				new ClassReader(classBytes).accept(classNode, 0);

				for (MethodNode m : classNode.methods) {
					if ("getModule".equals(m.name)) {
						for (AbstractInsnNode n : m.instructions) {
							if (n.getOpcode() == Opcodes.ARETURN) {
								InsnList insnList = new InsnList();
								LabelNode labelNode = new LabelNode();
								insnList.add(new LdcInsnNode("java.lang.$JaCoCo"));
								insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
								insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false));
								insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false));
								insnList.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
								insnList.add(new LdcInsnNode(Type.getType(Object.class)));
								insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getModule", "()Ljava/lang/Module;", false));
								insnList.add(new InsnNode(Opcodes.POP));
								insnList.add(labelNode);
								m.instructions.insertBefore(n, insnList);
							}
						}
					}
				}

				ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				classNode.accept(classWriter);
				return classWriter.toByteArray();
			}
		};
		instrumentation.addTransformer(retransformer, true);
		instrumentation.retransformClasses(Class.class);
		instrumentation.removeTransformer(retransformer);
	}

	@Override
	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		injected = Lookup //
				.privateLookupIn(locator, Lookup.lookup()) //
				.defineClass(createClass(injectedClassName)); //
		injected //
				.getField(FIELD_NAME) //
				.set(null, data);
		// https://github.com/jboss-modules/jboss-modules/blob/367d7479d23b165d2cf7e2419f31d78223b957a4/src/main/java/org/jboss/modules/JDKModuleFinder.java#L159
		System.out.println(getModule(injected));
		System.out.println(getModule(locator));
	}

	/**
	 * @return {@code cls.getModule()}
	 */
	private static Object getModule(final Class<?> cls) throws Exception {
		return Class.class //
			.getMethod("getModule") //
			.invoke(cls);
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, injectedClassName, FIELD_NAME,
				FIELD_TYPE);

		RuntimeData.generateAccessCall(classid, classname, probecount, mv);

		return 6;
	}

	private static byte[] createClass(final String name) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V9, Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC,
				name.replace('.', '/'), null, "java/lang/Object", null);
		cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FIELD_NAME,
				FIELD_TYPE, null, null);
		cw.visitEnd();
		return cw.toByteArray();
	}

	/**
	 * Provides access to classes {@code java.lang.invoke.MethodHandles} and
	 * {@code java.lang.invoke.MethodHandles.Lookup} introduced in Java 8.
	 */
	private static class Lookup {

		private final Object instance;

		private Lookup(final Object instance) {
			this.instance = instance;
		}

		/**
		 * @return a lookup object for the caller of this method
		 */
		static Lookup lookup() throws Exception {
			return new Lookup(Class //
					.forName("java.lang.invoke.MethodHandles") //
					.getMethod("lookup") //
					.invoke(null));
		}

		/**
		 * See corresponding method introduced in Java 9.
		 *
		 * @param targetClass
		 *            the target class
		 * @param lookup
		 *            the caller lookup object
		 * @return a lookup object for the target class, with private access
		 */
		static Lookup privateLookupIn(final Class<?> targetClass,
				final Lookup lookup) throws Exception {
			return new Lookup(Class //
					.forName("java.lang.invoke.MethodHandles") //
					.getMethod("privateLookupIn", Class.class,
							Class.forName(
									"java.lang.invoke.MethodHandles$Lookup")) //
					.invoke(null, targetClass, lookup.instance));
		}

		/**
		 * See corresponding method introduced in Java 9.
		 *
		 * @param bytes
		 *            the class bytes
		 * @return class
		 */
		Class<?> defineClass(final byte[] bytes) throws Exception {
			return (Class<?>) Class //
					.forName("java.lang.invoke.MethodHandles$Lookup")
					.getMethod("defineClass", byte[].class)
					.invoke(this.instance, new Object[] { bytes });
		}

	}

}
