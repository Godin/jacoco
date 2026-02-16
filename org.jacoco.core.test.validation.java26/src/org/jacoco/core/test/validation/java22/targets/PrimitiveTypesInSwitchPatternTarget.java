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
 * TODO https://openjdk.org/jeps/530
 */
public class PrimitiveTypesInSwitchPatternTarget {

	record Box(double c) {
	}

	/**
	 * TODO expected 2 branches? but currently 2 + 2
	 */
	private static void instanceofNestedPrimitivePatternUnconditionalConversion(
			Object o) {
		if (o instanceof Box(double i)) { // assertFullyCovered(1, 3)
			nop("Box(double)"); // assertFullyCovered()
		} // assertEmpty()
	}

	/**
	 * TODO expected 4 or 2?
	 */
	private static void instanceofNestedPrimitivePatternConditionalConversion(
			Object o) {
		if (o instanceof Box(int i)) { // assertFullyCovered(1, 3)
			nop("Box(int)"); // assertFullyCovered()
		} // assertEmpty()
	}

	/**
	 * TODO expected 2 branches? but currently 2 + 2
	 */
	private static void switchNestedPrimitivePatternUnconditionalConversion(
			Object o) {
		switch (o) { // assertFullyCovered(1, 1)
		case Box(double d) -> // assertPartlyCovered(1, 1)
			nop("Box(double)"); // assertFullyCovered()
		default -> // assertEmpty()
			nop("default"); // assertPartlyCovered()
		} // assertEmpty()
	}

	/**
	 * TODO expected 3 branches? but currently 3 + 2 + 2
	 */
	private static void switchNestedPrimitivePatternConditionalConversion(
			Object o) {
		switch (o) { // assertFullyCovered(0, 3)
		case Box(int i) -> // assertFullyCovered(0, 2)
			nop("Box(int)"); // assertFullyCovered()
		case Box(double d) -> // assertPartlyCovered(1, 1)
			nop("Box(double)"); // assertFullyCovered()
		default -> // assertEmpty()
			nop("default"); // assertFullyCovered()
		} // assertEmpty()
	}

	/**
	 * TODO expected 4 branches? but currently 4 + 2 + 2
	 */
	private static void example(Object o) {
		switch (o) { // assertFullyCovered(0, 4)
		case Box(int i) -> // assertFullyCovered(0, 2)
			nop("Box(int)"); // assertFullyCovered()
		case String s -> // assertFullyCovered()
			nop("String"); // assertFullyCovered()
		case Box(double d) -> // assertPartlyCovered(1, 1)
			nop("Box(double)"); // assertFullyCovered()
		default -> // assertEmpty()
			nop("default"); // assertFullyCovered()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		instanceofNestedPrimitivePatternUnconditionalConversion(new Box(1.2));
		instanceofNestedPrimitivePatternUnconditionalConversion(new Object());

		instanceofNestedPrimitivePatternConditionalConversion(new Box(1));
		instanceofNestedPrimitivePatternConditionalConversion(new Object());

		switchNestedPrimitivePatternUnconditionalConversion(new Box(1.2));

		switchNestedPrimitivePatternConditionalConversion(new Box(1));
		switchNestedPrimitivePatternConditionalConversion(new Box(1.2));
		switchNestedPrimitivePatternConditionalConversion(new Object());

		example(new Box(1));
		example("");
		example(new Box(1.2));
		example(new Object());
	}

}
