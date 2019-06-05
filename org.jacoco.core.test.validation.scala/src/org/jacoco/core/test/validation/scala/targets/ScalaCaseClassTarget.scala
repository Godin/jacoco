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

/**
 * Test target for <code>case</code> classes.
 */
object ScalaCaseClassTarget {

  case class CaseClass( // assertPartlyCovered(17, 0)
                        a: Int, // assertPartlyCovered(0, 0)
                        b: Int // assertPartlyCovered(0, 0)
                      ) // assertEmpty()

  def main(args: Array[String]): Unit = {
    CaseClass(1, 2)
  }

}
