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

import org.jacoco.core.test.validation.targets.Stubs.{f, i2, nop, t}

/**
 * Test target for Scala control structures.
 */
object ScalaControlStructuresTarget {

  private def test(): Unit = {
    if (t()) {
      nop()
    } else {
      nop() /* TODO this line shows up in debugger */
    }
  }

  private def missedIfBlock(): Unit = {

    if (f()) { // assertFullyCovered(1, 1)
      nop() // assertNotCovered()
    } else {
      nop() // assertFullyCovered()
    }

  }

  private def executedIfBlock(): Unit = {

    if (t()) { // assertFullyCovered(1, 1)
      nop() // assertFullyCovered()
    } else {
      nop() // assertNotCovered()
    }
    return /* TODO */

  }

  private def missedWhileBlock(): Unit = {

    while (f()) { // assertFullyCovered(1, 1)
      nop() // assertNotCovered()
    }

  }

  private def executedWhileBlock(): Unit = {

    var i = 0
    while (i < 3) { // assertFullyCovered(0, 2)
      i = i + 1 // assertFullyCovered()
    }

  }

  private def executedDoWhileBlock(): Unit = {

    do {
      nop() // assertFullyCovered()
    } while (f()) // assertFullyCovered(1, 1)

  }

  private def missedForBlock(): Unit = {

    for (_ <- 1 to 0) { // assertFullyCovered()
      nop() // assertNotCovered()
    }

  }

  private def executedForBlock(): Unit = {

    for (_ <- 0 to 1) { // assertFullyCovered()
      nop() // assertFullyCovered()
    }

  }

  private def switch(): Unit = {

    i2() match { // assertFullyCovered(2, 1)
      case 1 => nop() // assertNotCovered()
      case 2 => nop() // assertFullyCovered()
      case _ => nop() // assertNotCovered()
    }
    return /* TODO */

  }

  private def conditionalReturn(): Unit = {

    if (t()) {
      return // assertFullyCovered()
    }
    nop() // assertNotCovered()

  }

  private def implicitReturn(): Unit = { // assertFullyCovered()
  } // assertEmpty()

  private def explicitReturn(): Unit = { // assertEmpty()

    return // assertFullyCovered()

  } // assertEmpty()

  def main(args: Array[String]): Unit = {
    test()
    missedIfBlock()
    executedIfBlock()
    missedWhileBlock()
    executedWhileBlock()
    executedDoWhileBlock()
    missedForBlock()
    executedForBlock()
    switch()
    conditionalReturn()
    implicitReturn()
    explicitReturn()
  }

}
