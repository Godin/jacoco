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
package org.jacoco.core.test.validation.scala.targets

object ScalaSyntheticAccessorsTarget {

  class Outer {
    private var x = 0 // assertPartlyCovered()

    class Inner {
      def example() {
        x += 1 // assertNotCovered()
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val outer = new Outer()
    val inner = new outer.Inner()
  }

}
