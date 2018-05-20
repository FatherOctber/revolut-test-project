package com.fatheroctober.moneytransfer

import com.fatheroctober.moneytransfer.domain.Domain.Account
import com.fatheroctober.moneytransfer.domain.{AccountService, ExchangeRateService}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.{BeforeAndAfter, FunSuiteLike}
import org.scalatest.mockito.MockitoSugar
import org.scalatra.test.scalatest.ScalatraSuite

import scala.collection.mutable.{Map => MMap}


class AccountControllerTest extends ScalatraSuite with FunSuiteLike with BeforeAndAfter with MockitoSugar {
  val accountServiceMock = mock[AccountService]
  val exchangeRateServiceMock = mock[ExchangeRateService]
  val storage = MMap(
    TestBase.rurAccount.number -> TestBase.rurAccount,
    TestBase.usdAccount.number -> TestBase.usdAccount)

  addServlet(new AccountController(accountServiceMock, exchangeRateServiceMock), "/*")

  before {
    Mockito.when(accountServiceMock.getAccount(ArgumentMatchers.any(classOf[String]))) then ((i) => {
      val key = i.getArguments()(0)
      storage.get(key.asInstanceOf[String]).orNull
    })

    Mockito.when(accountServiceMock.saveAccount(ArgumentMatchers.any(classOf[Account]))) then ((i) => {
      val account = i.getArguments()(0).asInstanceOf[Account]
      storage += (account.number -> account)
    })
  }

  test("get account successfully") {
    get("/" + TestBase.rurAccount.number) {
      status should equal(200)
      body should include(loadResource("data/getRs.json"))
    }
  }

  test("create account successfully") {
    post("/create", loadResource("data/createRq.json").getBytes()) {
      status should equal(200)
    }

    get("/petr1") {
      status should equal(200)
      body should include(loadResource("data/createRs.json"))
    }
  }

  test("create account error") {
    post("/create", loadResource("data/createErrorRq.json").getBytes()) {
      status should equal(400)
      body should include(loadResource("data/createErrorRs.json"))
    }
  }

  test("topup successfully") {
    post("/" + TestBase.rurAccount.number + "/topup", loadResource("data/transactionRq.json").getBytes()) {
      status should equal(200)
    }
  }

  test("withdrawal successfully") {
    post("/" + TestBase.rurAccount.number + "/withdraw", loadResource("data/transactionRq.json").getBytes()) {
      status should equal(200)
    }
  }

  def loadResource(filename: String): String = {
    val source = scala.io.Source.fromURL(getClass.getResource(filename))
    try source.mkString finally source.close()
  }
}
