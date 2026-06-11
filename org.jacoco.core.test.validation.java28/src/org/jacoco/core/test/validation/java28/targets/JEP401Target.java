/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java28.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * <a href="https://openjdk.org/jeps/401">JEP 401</a>)
 */
public class JEP401Target {

	private value record R(int x) { // assertFullyCovered()
	} // assertEmpty()

	private static value class C { // assertEmpty()
		private int x; // assertEmpty()
		C(int x) { // assertFullyCovered()
			this.x = x; // assertFullyCovered()
		} // assertFullyCovered()
	} // assertEmpty()

	private static value class StrictInit {
		private int v;
		StrictInit(int v) {
			if (v < 0) {
				this.v = -v;
			} else {
				this.v = v;
			}
		}
	}

	public static void main(String[] args) {
		new R(1);
		new C(1);
		new StrictInit(1);
	}

}
