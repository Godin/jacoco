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

import static org.junit.Assert.assertEquals;

import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Unit tests for {@link LocalProbeArrayStrategy}.
 */
public class LocalProbeArrayStrategyTest {

	private LocalProbeArrayStrategy strategy;

	@Before
	public void setup() {
		strategy = new LocalProbeArrayStrategy("ClassName", 1L, 1,
				new OfflineInstrumentationAccessGenerator());
	}

	@Test
	public void should_store_instance() {
		final MethodNode m = new MethodNode();
		final int maxStack = strategy.storeInstance(m, false, 13);

		assertEquals(4, maxStack);

		assertEquals(5, m.instructions.size());

		final AbstractInsnNode i = m.instructions.getLast();
		assertEquals(Opcodes.ASTORE, i.getOpcode());
		assertEquals(13, ((VarInsnNode) i).var);
	}

	@Test
	public void should_not_add_memebers() {
		final ClassNode c = new ClassNode();
		strategy.addMembers(c, 0);

		assertEquals(0, c.methods.size());
		assertEquals(0, c.fields.size());
	}

}
