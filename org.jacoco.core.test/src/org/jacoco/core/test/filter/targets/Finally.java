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
import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

public class Finally {

	private static void tryCatchFinally(final boolean t) {
		try {
			ex(t);
		} catch (Exception e) {
			nop();
		} finally { // $line-finally$
			nop();
		} // $line-finallyEnd$
	}

	public static void main(String[] args) {
		tryCatchFinally(false);
		tryCatchFinally(true);
	}

}
