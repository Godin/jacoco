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
package org.jacoco.core.test.validation.java23.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * TODO
 * mvn clean package -Dbytecode.version=23 -pl org.jacoco.core.test.validation.java23
 */
public class WipTarget {

	private static void example(Object o) {
		if (o instanceof int i) { // assertFullyCovered(1, 5)
			nop(i); // assertFullyCovered()
		} else {
			nop(); // assertFullyCovered()
		}
	}

	private static void example2(Object b) {
		switch (b) { // assertFullyCovered(0, 3)
		case boolean x when x == true -> nop(b); // assertFullyCovered(0, 2)
		case boolean x-> nop(b); // assertFullyCovered()
		default -> nop();
		}
	}

	record JsonNumber(double d) {
	}

	private static void example3(Object o) {
		switch(o) {
		case JsonNumber(int i) -> nop(i);
		default -> nop();
		}
	}

	public static void main(String[] args) {
		example(new Object());
		example(1);

		example2("");
		example2(true);
		example2(false);
	}


}
