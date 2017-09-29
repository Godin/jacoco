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
	private static void motivation(boolean t) {
		try {
			ex(t);
		} finally {
			if (!t) { // $line-motivation.if$
				nop();
			}
		}
	}

	private static void test() {
		try {
			nop();
		} finally {
			nop(); // $line-test.finallyBlock$
		}
		nop(); // $line-test.after$
	}

	private static void catchFinally() {
		try {
			nop("try");
		} catch (Exception e) {
			nop("catch");
		} finally {
			nop("finally"); // $line-catchFinally.finallyBlock$
		}
	}

	/**
	 * <pre>
	 *   InputStream in = null;
	 *   try {
	 *     in = ...;
	 *     OutputStream out = null;
	 *     try {
	 *       out = ...;
	 *       ...
	 *     } finally {
	 *       if (out != null) {
	 *         out.close();
	 *       }
	 *     }
	 *   } finally {
	 *     if (in != null) {
	 *       in.close();
	 *     }
	 *   }
	 * </pre>
	 */
	private static void xxx(boolean t1, boolean t2) {
		try {
			ex(t1);
			try {
				ex(t2);
			} finally {
				if (!t2) { // $line-xxx$
					nop();
				}
			}
		} finally {
			if (!t1) {
				nop();
			}
		}
	}

	/**
	 * TODO comment
	 */
	private static void nested() {
		try {
			nop("try");
		} finally {
			try {
				nop("finally.try");
			} finally {
				nop("finally.finally"); // $line-nested$
			}
		}
	}

	/**
	 * Note that in this case javac does not assign line number to
	 * <code>goto</code> instruction generated for <code>break</code> statement
	 * (see <a href=
	 * "https://bugs.openjdk.java.net/browse/JDK-8180141">JDK-8180141</a>).
	 */
	private static void wip() {
		do {
			try {
				if (t()) {
					break; // $line-wip$
				}
				nop();
			} finally {
				nop("finally"); // $line-wip.finallyBlock$
			}
		} while (t());
	}

	/**
	 * Note that difference of this case from {@link #wip()} is that there is
	 * nothing between {@code break} statement and end of {@code try} block.
	 */
	private static void wip2() {
		// TODO
		do {
			try {
				if (t()) {
					break;
				}
			} finally {
				nop("finally"); // $line-wip2$
			}
		} while (t());
	}

	/**
	 * Note that javac generates <code>goto</code> instruction that refers to a
	 * line of previous instruction. And so causes partial coverage of last line
	 * of finally handler, when <code>while</code> loop executed only once.
	 */
	private static void beforeEndOfWhile() {
		while (t()) {
			try {
				ex();
			} finally {
				nop(); // $line-beforeEndOfWhile.finallyBlock.1$
				nop(); // $line-beforeEndOfWhile.finallyBlock.2$
			}
		}
	}

	private static void beforeEndOfDo() {
		do {
			try {
				ex();
			} finally {
				nop(); // $line-beforeEndOfDo.finallyBlock$
			}
		} while (t());
	}

	private static void alwaysCompletesAbruptly() {
		try {
			nop();
		} finally {
			return; // $line-alwaysCompletesAbruptly$
		}
	}

	public static void main(String[] args) {
		try {
			beforeEndOfWhile();
		} catch (Exception ignore) {
		}
		try {
			beforeEndOfDo();
		} catch (Exception ignore) {
		}

		wip();
		wip2();

		catchFinally();

		xxx(false, false);
		// try {
		// xxx(false, true);
		// } catch (Exception ignore) {
		// }
		// try {
		// xxx(true, true);
		// } catch (Exception ignore) {
		// }

		motivation(false);
		try {
			motivation(true);
		} catch (Exception ignore) {
		}

		test();
		nested();
		alwaysCompletesAbruptly();

		emptyCatch();
	}

	private static void emptyCatch() {
		try {
			nop("try");
		} catch (Exception ignore) {
		} finally {
			nop("finally"); // $line-emptyCatchExecuted.finallyBlock$
		}
	}

}
