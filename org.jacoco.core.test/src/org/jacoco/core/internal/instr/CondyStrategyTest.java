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
package org.jacoco.core.internal.instr;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.junit.Test;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link CondyStrategy}.
 */
public class CondyStrategyTest {

	private final CondyStrategy strategy = new CondyStrategy("ClassName", 1L,
			false, new IExecutionDataAccessorGenerator() {
				public int generateDataAccessor(final long classid,
						final String classname, final int probecount,
						final MethodVisitor mv) {
					return 1;
				}
			});

	@Test
	public void storeInstance() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION);

		final int requiredStackSize = strategy.storeInstance(m, false, 1);
		assertEquals(1, requiredStackSize);

		final ConstantDynamic cd = (ConstantDynamic) ((LdcInsnNode) m.instructions
				.get(0)).cst;
		assertEquals("ClassName", cd.getBootstrapMethod().getOwner());
		assertEquals("$jacocoInit", cd.getBootstrapMethod().getName());
	}

	@Test
	public void addMembers() {
		final ClassNode c = new ClassNode(InstrSupport.ASM_API_VERSION);

		strategy.addMembers(c, 1);

		assertEquals(1, c.methods.size());

		final MethodNode m = c.methods.get(0);
		assertEquals("$jacocoInit", m.name);
		assertEquals(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE
				| Opcodes.ACC_STATIC, m.access);
		assertEquals(1, m.maxStack);
		assertEquals(3, m.maxLocals);
	}

}
