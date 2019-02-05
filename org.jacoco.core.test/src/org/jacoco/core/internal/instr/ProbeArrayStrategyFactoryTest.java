/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ProbeArrayStrategyFactory} and the
 * {@link IProbeArrayStrategy} implementations. The verifies the behaviour of
 * the returned {@link IProbeArrayStrategy} instances for different classes.
 */
public class ProbeArrayStrategyFactoryTest {

	private IExecutionDataAccessorGenerator generator = new IExecutionDataAccessorGenerator() {
		public int generateDataAccessor(final long classid,
				final String classname, final int probecount,
				final MethodVisitor mv) {
			assertEquals("Foo", classname);
			assertEquals(42, classid);
			return 0;
		}
	};

	private ClassNode c = new ClassNode();

	@Test
	public void testClass1() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_1, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass2() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_2, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass3() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_3, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass4() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_4, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass5() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_5, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(false);
	}

	@Test
	public void testClass6() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_6, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testClass7() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_7, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testClass8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8, 0, false, true,
				true);
		assertEquals(ClassFieldProbeArrayStrategy.class, strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testInterface7() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_7,
				Opcodes.ACC_INTERFACE, true, false, true);
		assertEquals(LocalProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void testEmptyInterface7() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_7,
				Opcodes.ACC_INTERFACE, false, false, false);
		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyInterface7StoreInstance() {
		IProbeArrayStrategy strategy = test(Opcodes.V1_7, Opcodes.ACC_INTERFACE,
				false, false, false);
		strategy.storeInstance(null, false, 0);
	}

	@Test
	public void testInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, false, true, true);
		assertEquals(InterfaceFieldProbeArrayStrategy.class,
				strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_INTF_ACC);

		assertEquals(2, c.methods.size());

		MethodNode m = c.methods.get(c.methods.size() - 2);
		assertEquals(InstrSupport.INITMETHOD_NAME, m.name);
		assertEquals(InstrSupport.INITMETHOD_DESC, m.desc);
		assertEquals(InstrSupport.INITMETHOD_ACC, m.access);
		assertTrue(hasFrames(m));

		m = c.methods.get(c.methods.size() - 1);
		assertEquals(InstrSupport.CLINIT_NAME, m.name);
		assertEquals(InstrSupport.CLINIT_DESC, m.desc);
		assertEquals(InstrSupport.CLINIT_ACC, m.access);
	}

	@Test
	public void testEmptyInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, false, false, false);
		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyInterface8StoreInstance() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, false, false, false);
		strategy.storeInstance(null, false, 0);
	}

	@Test
	public void testClinitAndAbstractMethodsInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, true, false, true);
		assertEquals(LocalProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void testClinitInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, true, false, false);
		assertEquals(LocalProbeArrayStrategy.class, strategy.getClass());
		assertNoDataField();
		assertNoInitMethod();
	}

	@Test
	public void testClinitAndMethodsInterface8() {
		final IProbeArrayStrategy strategy = test(Opcodes.V1_8,
				Opcodes.ACC_INTERFACE, true, true, true);
		assertEquals(InterfaceFieldProbeArrayStrategy.class,
				strategy.getClass());
		assertDataField(InstrSupport.DATAFIELD_INTF_ACC);
		assertInitMethod(true);
	}

	@Test
	public void testModule() {
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null,
				null);
		writer.visitModule("module", 0, null).visitEnd();
		writer.visitEnd();

		final IProbeArrayStrategy strategy = ProbeArrayStrategyFactory
				.createFor(0, new ClassReader(writer.toByteArray()), generator);
		assertEquals(NoneProbeArrayStrategy.class, strategy.getClass());
	}

	private IProbeArrayStrategy test(int version, int access, boolean clinit,
			boolean method, boolean abstractMethod) {
		ClassNode c = new ClassNode();
		c.version = version;
		c.access = access;
		c.name = "Foo";
		if (clinit) {
			MethodNode m = new MethodNode(0, "<clinit>", "()V", null, null);
			m.instructions.add(new InsnNode(Opcodes.RETURN));
			c.methods.add(m);
		}
		if (method) {
			MethodNode m = new MethodNode(0, "m", "()V", null, null);
			m.instructions.add(new InsnNode(Opcodes.RETURN));
			c.methods.add(m);
		}
		if (abstractMethod) {
			MethodNode m = new MethodNode(Opcodes.ACC_ABSTRACT, "m", "()V",
					null, null);
			c.methods.add(m);
		}

		final ClassWriter writer = new ClassWriter(0);
		c.accept(writer);

		IProbeArrayStrategy strategy = ProbeArrayStrategyFactory.createFor(42,
				new ClassReader(writer.toByteArray()), generator);

		for (MethodNode m : c.methods) {
			strategy.storeInstance(m, "<clinit>".equals(m.name), 42);
		}

		strategy.addMembers(this.c, 42);

		return strategy;
	}

	void assertDataField(int access) {
		assertEquals(1, c.fields.size());
		FieldNode f = c.fields.get(0);
		assertEquals(InstrSupport.DATAFIELD_NAME, f.name);
		assertEquals(access, f.access);
	}

	void assertNoDataField() {
		assertEquals(0, c.fields.size());
	}

	void assertInitMethod(boolean frames) {
		assertEquals(1, c.methods.size());
		MethodNode m = c.methods.get(0);
		assertEquals(InstrSupport.INITMETHOD_NAME, m.name);
		assertEquals(InstrSupport.INITMETHOD_DESC, m.desc);
		assertEquals(InstrSupport.INITMETHOD_ACC, m.access);
		assertEquals(frames, hasFrames(m));
	}

	void assertNoInitMethod() {
		assertEquals(0, c.methods.size());
	}

	private static boolean hasFrames(MethodNode m) {
		for (AbstractInsnNode i = m.instructions.getFirst(); i != null; i = i
				.getNext()) {
			if (i.getType() == AbstractInsnNode.FRAME) {
				return true;
			}
		}
		return false;
	}

}
