package com.revolut.intro

class ScalaIntro {

  def hello(): String = {
    var hello = "Hello World"
    println(hello)
    hello
  }

  def sum(a: Int, b: Int): Int = {
    a + b
  }

  def callbackFor(func: (Int, Int) => Int, a: Int, b: Int): Int = {
    func(a, b)
  }

}
