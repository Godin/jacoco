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
package org.jacoco.core.test.validation.java5.targets;

import org.jacoco.core.test.validation.targets.Stubs;

/**
 * TODO find better name
 */
public class WipTarget {

	/**
	 * TODO unfortunate side effect - partial coverage in non-throwing try-body
	 *
	 * IDEA: limit impact to methods with monitorenter instructions
	 *
	 * IDEA2: limit impact to instructions with non-zero monitor stack by
	 * wrapping probes with zero monitor stack into try-catch-rethrow
	 */
	private static boolean non_throwing_try(boolean b) {
		try {
			return !b; // assertPartlyCovered(2, 0)
		} finally {
			Stubs.nop(); // assertFullyCovered()
		}
	}

	/**
	 * TODO unfortunate side effect - partial coverage in non-throwing
	 * synchronized-body
	 */
	private static boolean non_throwing_synchronized(boolean b) {
		synchronized (new Object()) {
			return !b; // assertPartlyCovered(2, 0)
		}
	}

	private static boolean throwing_synchronized(boolean b) {
		synchronized (new Object()) {
			Stubs.nop(); // assertFullyCovered()
			return !b; // assertFullyCovered(0, 2)
		}
	}

	public static void main(String[] args) {
		Stubs.nop(non_throwing_try(true));
		Stubs.nop(non_throwing_try(false));

		Stubs.nop(non_throwing_synchronized(true));
		Stubs.nop(non_throwing_synchronized(false));

		Stubs.nop(throwing_synchronized(true));
		Stubs.nop(throwing_synchronized(false));
	}

}
