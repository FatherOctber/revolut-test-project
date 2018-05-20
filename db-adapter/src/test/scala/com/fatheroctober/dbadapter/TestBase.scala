package com.fatheroctober.dbadapter

object TestBase {

  case class SomeObj(name: String, value: Long) {
    def this() = this("none", 0)
  }

}
