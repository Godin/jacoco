package org.jacoco.core.test.validation.scala.targets

object ScalaCaseClassTarget {

  case class CaseClass // assertNotCovered(21, 0)
  ( // assertFullyCovered()
    i: Int,
    s: String // assertPartlyCovered()
  ) {
    def t(): Unit = {
      var m = reflect.runtime.universe.runtimeMirror(this.getClass.getClassLoader)
      println(m.reflect(this).symbol.asClass.isCaseClass)
    }
  } // assertEmpty()

  def main(args: Array[String]): Unit = {
    CaseClass(42, "foo").t()
  }

}
