/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java5;

import java.io.IOException;

import org.jacoco.core.internal.instr.Monitors;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.WipTarget;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Test of code coverage in {@link WipTarget}.
 */
public class WipTest extends ValidationTestBase {
	public WipTest() {
		super(WipTarget.class);
	}

	@Test
	public void test() throws IOException {
		byte[] classBytes = TargetLoader.getClassDataAsBytes(WipTarget.class);
		ClassNode classNode = classBytesToClassNode(classBytes);
		for (MethodNode m : classNode.methods) {
			System.out.println(m.name);
			Monitors monitors = new Monitors(m);
			for (int i = 0; i < m.instructions.size(); i++) {
				System.out.print(i + " ");
				Monitors.Stack stack = monitors.stackAt(i);
				if (stack == Monitors.EMPTY) {
					System.out.print("E");
				} else if (stack == Monitors.UNREACHABLE) {
					System.out.print("U");
				} else {
					System.out.print("N");
				}
				System.out.print(" ");
				System.out.println(m.instructions.get(i) + " ");
			}
		}
	}

	public static ClassNode classBytesToClassNode(final byte[] classBytes) {
		final ClassNode classNode = new ClassNode();
		new ClassReader(classBytes).accept(classNode, ClassReader.SKIP_FRAMES);
		return classNode;
	}

}
