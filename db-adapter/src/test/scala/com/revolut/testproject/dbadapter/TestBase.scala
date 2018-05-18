package com.revolut.testproject.dbadapter

object TestBase {

  case class SomeObj(name: String, value: Long) {
    def this() = this("none", 0)
  }

}
