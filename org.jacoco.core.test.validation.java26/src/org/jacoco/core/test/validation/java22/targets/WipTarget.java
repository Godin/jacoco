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
package org.jacoco.core.test.validation.java22.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * TODO
 */
public class WipTarget {

	private static void wip(String s) {
		switch (s) {
		default -> nop("default");
		}

		switch (0) {
		case 1 -> nop("1");
		default -> nop("default");
		}

		switch ("") {
		default -> nop("default");
		}
	}

	private static void e(byte b, int i) {
		/**
		 * According to
		 * https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.29
		 * constant expression can not contain instanceof operator
		 */
		if (Byte.MAX_VALUE instanceof int) { // assertNotCovered(2, 0)
			nop("toto"); // assertNotCovered()
		} // assertEmpty()

		/* instanceof primitive type check */

		/* unconditionally exact conversion */
		if (b instanceof int) { // assertNotCovered(2, 0)
			nop("int"); // assertNotCovered()
		} // assertEmpty()

		/* instanceof primitive pattern top level unconditional conversion */

		if (b instanceof int byteToInt // assertNotCovered(4, 0)
				&& byteToInt == 42) { // assertEmpty()
			nop("42"); // assertNotCovered()
		} // assertEmpty()

		switch (b) { // assertNotCovered(0, 0)
		/* next LINENUMBER is ALOAD ASTORE */
		case int byteToInt -> // assertNotCovered()
			nop("int"); // assertNotCovered()
		} // assertEmpty()

		/* the above is similar to the below which is not about JEP 520 */
		switch (b) { // assertNotCovered(0, 0)
		default -> // assertEmpty()
			nop("default"); // assertNotCovered()
		} // assertEmpty()

		/* instanceof primitive type check conditional conversion */

		/* java/lang/runtime/ExactConversionsSupport.isIntToByteExact */
		if (i instanceof byte) { // assertNotCovered(2, 0)
			nop("byte"); // assertNotCovered()
		} // assertEmpty()

		/* instanceof primitive pattern top level conditional conversion */

		if (i instanceof byte intToByte // assertNotCovered(4, 0)
				&& intToByte == 42) { // assertEmpty()
			nop("42"); // assertNotCovered()
		} // assertEmpty()

		/* switch primitive pattern top level */

		switch (i) { // assertNotCovered(3, 0)
		case byte intToByte -> // assertNotCovered()
			nop("byte"); // assertNotCovered()
		case int intToInt // assertNotCovered(2, 0)
		when intToInt > 0 -> // assertEmpty()
			nop("int"); // assertNotCovered()
		case int intToInt -> // assertNotCovered()
			nop("int"); // assertNotCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
	}

}
