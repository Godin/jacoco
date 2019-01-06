/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.runtime;

import static java.lang.String.format;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This {@link IRuntime} implementation works with a modified system class. A
 * new static field is added to a bootstrap class that will be used by
 * instrumented classes. As the system class itself needs to be instrumented
 * this runtime requires a Java agent.
 */
public class ModifiedSystemClassRuntime extends AbstractRuntime {

	private static final String ACCESS_FIELD_TYPE = "Ljava/lang/Object;";

	private final Class<?> systemClass;

	private final String systemClassName;

	private final String accessFieldName;

	/**
	 * Creates a new runtime based on the given class and members.
	 * 
	 * @param systemClass
	 *            system class that contains the execution data
	 * @param accessFieldName
	 *            name of the public static runtime access field
	 * 
	 */
	public ModifiedSystemClassRuntime(final Class<?> systemClass,
			final String accessFieldName) {
		super();
		this.systemClass = systemClass;
		this.systemClassName = systemClass.getName().replace('.', '/');
		this.accessFieldName = accessFieldName;
	}

	@Override
	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		final Field field = systemClass.getField(accessFieldName);
		field.set(null, data);
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {

		mv.visitFieldInsn(Opcodes.GETSTATIC, systemClassName, accessFieldName,
				ACCESS_FIELD_TYPE);

		RuntimeData.generateAccessCall(classid, classname, probecount, mv);

		return 6;
	}

	/**
	 * Creates a new {@link ModifiedSystemClassRuntime} using the given class as
	 * the data container. Member is created with internal default name. The
	 * given class must not have been loaded before by the agent.
	 * 
	 * @param inst
	 *            instrumentation interface
	 * @param className
	 *            VM name of the class to use
	 * @return new runtime instance
	 * 
	 * @throws ClassNotFoundException
	 *             id the given class can not be found
	 */
	public static IRuntime createFor(final Instrumentation inst,
			final String className) throws ClassNotFoundException {
		return createFor(inst, className, "$jacocoAccess");
	}

	private static byte[] hexStringToByteArray(final String s) {
		final int len = s.length();
		final byte[] data = new byte[len / 3];
		for (int i = 0; i < len; i += 3) {
			data[i / 3] = (byte) ((Character.digit(s.charAt(i + 1), 16) << 4)
					+ Character.digit(s.charAt(i + 2), 16));
		}
		return data;
	}

	/**
	 * Creates a new {@link ModifiedSystemClassRuntime} using the given class as
	 * the data container. The given class must not have been loaded before by
	 * the agent.
	 * 
	 * @param inst
	 *            instrumentation interface
	 * @param className
	 *            VM name of the class to use
	 * @param accessFieldName
	 *            name of the added runtime access field
	 * @return new runtime instance
	 * 
	 * @throws ClassNotFoundException
	 *             id the given class can not be found
	 */
	public static IRuntime createFor(final Instrumentation inst,
			final String className, final String accessFieldName)
			throws ClassNotFoundException {
		final ClassFileTransformer transformer = new ClassFileTransformer() {
			public byte[] transform(final ClassLoader loader, final String name,
					final Class<?> classBeingRedefined,
					final ProtectionDomain protectionDomain,
					final byte[] source) throws IllegalClassFormatException {

				if (name.equals(className)) {
					if (name.equals("java/lang/UnknownError")) {
						// Dump of class from JDK 12, so that ASM can read it
						// jmod extract --dir 12 java.base.jmod
						// xxd -g 1 12/classes/java/lang/UnknownError.class
						byte[] bytes = hexStringToByteArray("" //
								+ " ca fe ba be 00 00 00 38 00 1a 0a 00 04 00 16 0a" //
								+ " 00 04 00 17 07 00 18 07 00 19 01 00 10 73 65 72" //
								+ " 69 61 6c 56 65 72 73 69 6f 6e 55 49 44 01 00 01" //
								+ " 4a 01 00 0d 43 6f 6e 73 74 61 6e 74 56 61 6c 75" //
								+ " 65 05 23 09 d6 74 32 ec 50 09 01 00 06 3c 69 6e" //
								+ " 69 74 3e 01 00 03 28 29 56 01 00 04 43 6f 64 65" //
								+ " 01 00 0f 4c 69 6e 65 4e 75 6d 62 65 72 54 61 62" //
								+ " 6c 65 01 00 12 4c 6f 63 61 6c 56 61 72 69 61 62" //
								+ " 6c 65 54 61 62 6c 65 01 00 04 74 68 69 73 01 00" //
								+ " 18 4c 6a 61 76 61 2f 6c 61 6e 67 2f 55 6e 6b 6e" //
								+ " 6f 77 6e 45 72 72 6f 72 3b 01 00 15 28 4c 6a 61" //
								+ " 76 61 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 29" //
								+ " 56 01 00 01 73 01 00 12 4c 6a 61 76 61 2f 6c 61" //
								+ " 6e 67 2f 53 74 72 69 6e 67 3b 01 00 0a 53 6f 75" //
								+ " 72 63 65 46 69 6c 65 01 00 11 55 6e 6b 6e 6f 77" //
								+ " 6e 45 72 72 6f 72 2e 6a 61 76 61 0c 00 0a 00 0b" //
								+ " 0c 00 0a 00 11 01 00 16 6a 61 76 61 2f 6c 61 6e" //
								+ " 67 2f 55 6e 6b 6e 6f 77 6e 45 72 72 6f 72 01 00" //
								+ " 1d 6a 61 76 61 2f 6c 61 6e 67 2f 56 69 72 74 75" //
								+ " 61 6c 4d 61 63 68 69 6e 65 45 72 72 6f 72 00 21" //
								+ " 00 03 00 04 00 00 00 01 00 1a 00 05 00 06 00 01" //
								+ " 00 07 00 00 00 02 00 08 00 02 00 01 00 0a 00 0b" //
								+ " 00 01 00 0c 00 00 00 33 00 01 00 01 00 00 00 05" //
								+ " 2a b7 00 01 b1 00 00 00 02 00 0d 00 00 00 0a 00" //
								+ " 02 00 00 00 2b 00 04 00 2c 00 0e 00 00 00 0c 00" //
								+ " 01 00 00 00 05 00 0f 00 10 00 00 00 01 00 0a 00" //
								+ " 11 00 01 00 0c 00 00 00 3e 00 02 00 02 00 00 00" //
								+ " 06 2a 2b b7 00 02 b1 00 00 00 02 00 0d 00 00 00" //
								+ " 0a 00 02 00 00 00 35 00 05 00 36 00 0e 00 00 00" //
								+ " 16 00 02 00 00 00 06 00 0f 00 10 00 00 00 00 00" //
								+ " 06 00 12 00 13 00 01 00 01 00 14 00 00 00 02 00" //
								+ " 15" //
						);
						try {
							System.out.println("INSTRUMENTING...");
							bytes = instrument(bytes, accessFieldName);
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println("INSTRUMENTED!!!");
						return bytes;
					}

					return instrument(source, accessFieldName);
				}
				return null;
			}
		};
		inst.addTransformer(transformer);
		final Class<?> clazz = Class.forName(className.replace('/', '.'));
		inst.removeTransformer(transformer);
		try {
			clazz.getField(accessFieldName);
		} catch (final NoSuchFieldException e) {
			throw new RuntimeException(
					format("Class %s could not be instrumented.", className),
					e);
		}
		return new ModifiedSystemClassRuntime(clazz, accessFieldName);
	}

	/**
	 * Adds the static data field to the given class definition.
	 * 
	 * @param source
	 *            class definition source
	 * @param accessFieldName
	 *            name of the runtime access field
	 * @return instrumented version with added members
	 */
	public static byte[] instrument(final byte[] source,
			final String accessFieldName) {
		final ClassReader reader = new ClassReader(source);
		final ClassWriter writer = new ClassWriter(reader, 0);
		reader.accept(new ClassVisitor(InstrSupport.ASM_API_VERSION, writer) {

			@Override
			public void visitEnd() {
				createDataField(cv, accessFieldName);
				super.visitEnd();
			}

		}, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	private static void createDataField(final ClassVisitor visitor,
			final String dataField) {
		visitor.visitField(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC
						| Opcodes.ACC_TRANSIENT,
				dataField, ACCESS_FIELD_TYPE, null, null);
	}

}
