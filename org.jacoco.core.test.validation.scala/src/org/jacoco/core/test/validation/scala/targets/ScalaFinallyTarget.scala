/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.scala.targets

import org.jacoco.core.test.validation.targets.Stubs.nop

/**
 * TODO
 */
object ScalaFinallyTarget {

  def main(args: Array[String]): Unit = {
    try { // assertEmpty()
      nop() // assertFullyCovered()
    } finally { // assertEmpty()
      nop() // assertFullyCovered()
    } // assertEmpty()
  }

}
