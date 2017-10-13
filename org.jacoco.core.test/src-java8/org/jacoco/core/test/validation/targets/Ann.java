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
package org.jacoco.core.test.validation.targets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

public class Ann {

	@Target(ElementType.TYPE_USE)
	@interface Annotation {
	}

	private static abstract class A {
		public void m() {
			@Annotation
			Object x = new Object();
			nop(x);
		}
	}

	public static class B extends A {
	}

	public static void main(String[] args) {
		new B().m();
	}

}
