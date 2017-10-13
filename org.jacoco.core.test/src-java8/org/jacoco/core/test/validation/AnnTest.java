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
package org.jacoco.core.test.validation;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.targets.Ann;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class AnnTest extends ValidationTestBase {

	public AnnTest() throws Exception {
		super("src-java8", Ann.class);
	}

	@Test
	public void test() throws IOException {
		try {
			new ClassReader(TargetLoader.getClassDataAsBytes(Ann.B.class))
					.accept(new ClassNode(), 0);
			fail("ArrayIndexOutOfBoundsException expected");
		} catch (final ArrayIndexOutOfBoundsException e) {
			// expected
		}
	}

}
