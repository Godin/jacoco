package org.jacoco.core.test.validation.scala.targets

/**
 * Test target for methods with default arguments.
 */
object ScalaDefaultArgumentsTarget {

  def m(a: Boolean = false)(b: String = if (a) "a" else "b"): String = b  // assertFullyCovered(0, 2)

  def main(args: Array[String]): Unit = {
    m()()
    m(a = true)()
  }

}
