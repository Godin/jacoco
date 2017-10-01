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
package org.jacoco.core.test.filter.targets;

import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target is a finally block.
 */
public class Finally {

	/**
	 * <pre>
	 *   InputStream in = null;
	 *   try {
	 *     in = ...;
	 *     ...
	 *   } finally {
	 *     if (in != null) {
	 *       in.close();
	 *     }
	 *   }
	 * </pre>
	 */
	private static void test(boolean t) {
		try {
			ex(t);
		} finally {
			if (t) { // $line-test$
				nop("finally");
			}
		}
	}

	private static void alwaysCompletesAbruptly() {
		try {
			nop();
		} finally {
			return; // $line-alwaysCompletesAbruptly$
		}
	}

	public static void main(String[] args) {
		test(false);
		try {
			test(true);
		} catch (Exception ignore) {
		}

		alwaysCompletesAbruptly();
	}

}
