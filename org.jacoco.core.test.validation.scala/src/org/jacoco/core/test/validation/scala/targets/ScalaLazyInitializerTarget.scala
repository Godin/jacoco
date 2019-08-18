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
 * Test target for lazy initializers.
 */
object ScalaLazyInitializerTarget {

  private object BooleanBitmap {
    final lazy val field = { // assertFullyCovered(0, 0)
      "field" // assertFullyCovered()
    }
  }

  private object ByteBitmap {
    final lazy val field1 = { // assertFullyCovered(0, 0)
      "field1" // assertFullyCovered()
    }

    final lazy val field2 = { // assertFullyCovered(0, 0)
      "field2" // assertFullyCovered()
    }
  }

  def main(args: Array[String]): Unit = {
    BooleanBitmap.field
    ByteBitmap.field1
    ByteBitmap.field2
  }

}
