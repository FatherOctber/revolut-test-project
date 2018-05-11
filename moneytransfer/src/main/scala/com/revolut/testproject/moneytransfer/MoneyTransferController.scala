package com.revolut.testproject.moneytransfer

import org.scalatra.ScalatraServlet
import org.slf4j.LoggerFactory

class MoneyTransferController extends ScalatraServlet {
  val logger = LoggerFactory.getLogger(getClass)

  override def init(): Unit = {
    super.init()
    println("Money Transfer service started")
  }

  get("/") {
    logger.info("test echo was invoked")
    "Hello world"
  }

  get("/:name") {
    val name = params.getOrElse("name", "world")
    "Hello " + name
  }

}
