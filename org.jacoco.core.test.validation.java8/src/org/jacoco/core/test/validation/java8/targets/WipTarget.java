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
package org.jacoco.core.test.validation.java8.targets;

import org.jacoco.core.test.validation.targets.Stubs;

import java.util.function.Supplier;

/**
 * TODO
 */
public class WipTarget {

	private static Boolean exec(Supplier<Boolean> supplier) {
		return supplier.get();
	}

	public static void main(String[] args) {
		if (Stubs.f() || Stubs.t()) {
			Stubs.nop();
		}
		if (exec(() -> Stubs.f() || Stubs.t()) || Stubs.f()) {
			Stubs.nop();
		}
		if (Stubs.f() || exec(() -> Stubs.f() || Stubs.t())) {
			Stubs.nop();
		}
	}

}
