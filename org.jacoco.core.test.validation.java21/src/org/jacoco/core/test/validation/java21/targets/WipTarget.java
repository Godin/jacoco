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
package org.jacoco.core.test.validation.java21.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 */
public class WipTarget {

	private record R(Object c) {
	}

	private static void switchStatement(Object o) {
		switch (o) {
		case R(Integer x) when x != null && x > 0 -> nop(x);
		case R(Integer x) -> nop(x);
		default -> nop();
		}
	}

	public static void main(String[] args) {
		switchStatement(new Object());
		switchStatement(new R(null));
	}

}
