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
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.StubException;
import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

public class Synchronized {

	private static final Object lock = new Object();

	private static void implicitException() {
		synchronized (lock) { // $line-implicitException.monitorenter$
			ex(); // $line-implicitException.exception$
		} // $line-implicitException.monitorexit$
	} // $line-implicitException.after$

	private static void explicitException() {
		synchronized (lock) { // $line-explicitException.monitorenter$
			throw new StubException(); // $line-explicitException.exception$
		} // $line-explicitException.monitorexit$
	} // $line-explicitException.after$

	public static void main(String[] args) {
		synchronized (lock) { // $line-monitorenter$
			nop();
		} // $line-monitorexit$

		try {
			explicitException();
		} catch (StubException e) {
		}

		try {
			implicitException();
		} catch (StubException e) {
		}
	}

}
