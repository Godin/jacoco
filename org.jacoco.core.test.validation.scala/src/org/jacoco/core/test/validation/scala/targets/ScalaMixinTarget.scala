package org.jacoco.core.test.validation.scala.targets

object ScalaMixinTarget {

  trait T {
    def t(): Unit = {} // assertNotCovered()
    def t$(): Unit = {} // assertNotCovered()
  }

  class B {
  }

  class C extends B with T { // assertPartlyCovered()

    def c(): Unit = {} // assertFullyCovered()

  }

  def main(args: Array[String]): Unit = {
    new C().c()
  }

}
