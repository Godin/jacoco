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
import static org.jacoco.core.test.validation.targets.Stubs.t;

public class FinallyWip {

	private static void wip(boolean t) {
		try {
			nop();
			if (t) {
				nop();
				return;
			} else {
				nop();
				return;
			}
		} finally {
			nop("finally"); // $line-wip$
		}
	}

	// TODO add to unit test?
	private static void noInstructionsAfterDuplicate() {
		while (true) {
			try {
				ex();
			} finally {
				synchronized (Finally.class) {
					nop();
				} // $line-noInstructionsAfterDuplicate.1$
			} // $line-noInstructionsAfterDuplicate.2$
		}
	}

	private static void nested() {
		try {
			nop();
		} finally {
			try {
				nop();
			} finally {  // $line-nested.0$
				nop(); // $line-nested.1$
			}  // $line-nested.2$
		} // $line-nested.3$
	}

	private static void insideWhile() {
		while (t()) {
			try {
				ex();
			} finally { // $line-insideWhile.0$
				nop(); // $line-insideWhile.1$
			} // $line-insideWhile.2$
		}
	}

	public static void main(String[] args) {
		wip(true);
		wip(false);
	}

}
