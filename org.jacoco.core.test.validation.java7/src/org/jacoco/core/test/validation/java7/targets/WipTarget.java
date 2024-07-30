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
package org.jacoco.core.test.validation.java7.targets;

import java.io.Closeable;

public class WipTarget {

	private static String example(int i) {
		try (Resource r = factory(i)) { // assertFullyCovered(0, 0)
			if (i == 3) {
				return "";
			}
			if (i == 2) {
				return "";
			}
			if (r == null) { // assertFullyCovered(0, 2)
				return ""; // assertFullyCovered()
			} // assertEmpty()
		} // assertPartlyCovered(3, 3)
		return "";
	}

	private static Resource factory(int i) {
		return i != 0 ? new Resource() : null;
	}

	static class Resource implements Closeable {
		@Override
		public void close() {
		}
	}

	public static void main(String[] args) {
		example(0);
		example(1);
		example(2);
		example(3);
	}

}
