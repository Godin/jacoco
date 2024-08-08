/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.instr;

import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link InterfaceFieldProbeArrayStrategy}.
 */
public class InterfaceFieldProbeArrayStrategyTest {

	private InterfaceFieldProbeArrayStrategy strategy;

	@Before
	public void setup() {
		strategy = new InterfaceFieldProbeArrayStrategy("ClassName", 1L, 1,
				new OfflineInstrumentationAccessGenerator());
	}

	@Test
	public void should_add_field() {
		final ClassNode c = new ClassNode();
		strategy.addMembers(c, 1);

		assertEquals(1, c.fields.size());
		final FieldNode field = c.fields.get(0);
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, field.access);
		assertEquals("$jacocoData", field.name);
		assertEquals("[Z", field.desc);
	}

	// TODO assertStoreInstance

	@Test
	public void should_add_clinit() {
		final ClassNode c = new ClassNode();
		strategy.storeInstance(
				c.visitMethod(Opcodes.ACC_PUBLIC, "name", "()V", null, null),
				false, 1);
		strategy.addMembers(c, 1);

		assertEquals(3, c.methods.size());
		assertInitMethod(c.methods.get(1));
		final MethodNode m = c.methods.get(2);
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC, m.access);
		assertEquals("<clinit>", m.name);
		assertEquals("()V", m.desc);
	}

	/**
	 * TODO note that this is not tested by {@link ProbeArrayStrategyFactoryTest}
	 */
	@Test
	public void should_update_existing_clinit() {
		final ClassNode c = new ClassNode();
		strategy.storeInstance(
				c.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
						"<clinit>", "()V", null, null),
				true, 1);
		strategy.addMembers(c, 1);

		assertEquals(2, c.methods.size());
		assertInitMethod(c.methods.get(1));
		// TODO assert clinit update
	}

	private void assertInitMethod(final MethodNode m) {
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE
				| Opcodes.ACC_STATIC, m.access);
		assertEquals("$jacocoInit", m.name);
		assertEquals("()[Z", m.desc);
		// TODO assert content
	}

}
