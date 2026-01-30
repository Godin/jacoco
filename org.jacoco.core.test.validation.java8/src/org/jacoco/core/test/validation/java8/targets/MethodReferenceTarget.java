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
package org.jacoco.core.test.validation.java8.targets;

import java.util.function.IntFunction;

/**
 * TODO
 */
public class MethodReferenceTarget {

	private static class Private {
	}

	public static class Public {
	}

	private static class Inner1 {
		void x() {
		}
	}

	private static class Inner2 extends Inner1 {
		@Override
		void x() {
			noexec(super::x); // assertPartlyCoveredECJ()
		}
	}

	private static void x() {
	}

	public static void main(String[] args) {
		noexec(int[]::new); // assertPartlyCoveredECJ()
		exec(int[]::new); // assertFullyCovered()

		noexec(Object[]::new); // assertPartlyCoveredECJ()
		exec(Object[]::new); // assertFullyCovered()

		noexec(Public::new); // assertFullyCovered()
		exec(Public::new); // assertFullyCovered()

		noexec(Private::new); // assertP()
		exec(Private::new); // assertFullyCovered()

		noexec(MethodReferenceTarget::x); // assertFullyCovered()
		new Inner2().x();
	}

	private static void noexec(IntFunction f) {
	}

	private static void exec(IntFunction f) {
		f.apply(0);
	}

	private static void noexec(Runnable r) {
	}

	private static void exec(Runnable r) {
		r.run();
	}

}
