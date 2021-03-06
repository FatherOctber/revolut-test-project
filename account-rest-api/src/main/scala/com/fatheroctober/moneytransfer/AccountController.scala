package com.fatheroctober.moneytransfer

import com.fatheroctober.dbadapter.utils.SerializationUtil
import com.fatheroctober.moneytransfer.domain.{AccountService, Domain, ExchangeRateService}
import com.fatheroctober.moneytransfer.error.Error._
import com.google.inject.Inject
import org.scalatra.ScalatraServlet
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

class AccountController @Inject()(accountService: AccountService, exchangeRateService: ExchangeRateService) extends ScalatraServlet {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def routeAddress(): String = "/accounts/*"

  override def init(): Unit = {
    super.init()
    exchangeRateService.setupRates()
    logger.info("Account controller started")
  }

  get("/echo") {
    logger.info("GET >> echo")
    "{\"controller\":\"Account Controller\""
  }

  get("/:accountNumber") {
    logger.info("Get >> get")
    val number = params.getOrElse("accountNumber", "null")
    val account = accountService.getAccount(number)
    if (account != null) Response.Success(SerializationUtil.toJson(account)) else Response.RequestedNotFound()
  }

  post("/create") {
    logger.info("POST >> create")
    val body = request.body

    Option(Try(
      SerializationUtil.fromJson[Domain.Account](body)).getOrElse(null)) match {
      case Some(account) => {
        try {
          accountService.saveAccount(account)
          Response.Okay()
        } catch errorHandling
      }
      case None => Response.BadRq(ErrorResponse(serializationError._1, "Can`t read account").toString)
    }
  }

  post("/:accountNumber/topup") {
    logger.info("POST >> topup")
    val body = request.body
    val targetNumber = params.getOrElse("accountNumber", "null")

    Option(Try(
      SerializationUtil.fromJson[Domain.Transaction](body)).getOrElse(null)) match {
      case Some(transaction) => {
        try {
          accountService.topup(targetNumber, transaction)
          Response.Okay()
        } catch errorHandling
      }
      case None => Response.BadRq(ErrorResponse(serializationError._1, "Can`t read transaction").toString)
    }
  }

  post("/:accountNumber/withdraw") {
    logger.info("POST >> withdraw")
    val body = request.body
    val sourceNumber = params.getOrElse("accountNumber", "null")

    Option(Try(
      SerializationUtil.fromJson[Domain.Transaction](body)).getOrElse(null)) match {
      case Some(transaction) => {
        try {
          accountService.withdraw(sourceNumber, transaction)
          Response.Okay()
        } catch errorHandling
      }
      case None => Response.BadRq(ErrorResponse(serializationError._1, "Can`t read transaction").toString)
    }
  }

}
