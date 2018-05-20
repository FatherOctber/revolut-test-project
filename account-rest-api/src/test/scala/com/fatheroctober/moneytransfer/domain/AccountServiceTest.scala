package com.fatheroctober.moneytransfer.domain

import java.nio.ByteBuffer

import com.fatheroctober.dbadapter.Persistence
import com.fatheroctober.moneytransfer.domain.Domain.{Currency, Transaction}
import com.fatheroctober.moneytransfer.error.{AccountNotFoundException, IncorrectTransactionException}
import com.fatheroctober.moneytransfer.{StorageKeeper, TestBase}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FeatureSpec, GivenWhenThen}

import scala.collection.mutable

class AccountServiceTest extends FeatureSpec with GivenWhenThen with BeforeAndAfter with MockitoSugar {
  var map = new mutable.HashMap[ByteBuffer, ByteBuffer]()
  val dbMock = mock[Persistence[Array[Byte], Array[Byte]]]
  var exRateService: ExchangeRateService = null
  var storage: StorageKeeper.PersistenceJockey = null
  var accountService: AccountService = null

  info("Account service unit tests")

  before {
    Mockito.when(dbMock << (ArgumentMatchers.any(classOf[Array[Byte]]), ArgumentMatchers.any(classOf[Array[Byte]])))
      .thenAnswer((i) => {
        val key = ByteBuffer.wrap(i.getArguments()(0).asInstanceOf[Array[Byte]])
        val value = ByteBuffer.wrap(i.getArguments()(1).asInstanceOf[Array[Byte]])

        map.put(key, value)
      })

    Mockito.when(dbMock >> ArgumentMatchers.any(classOf[Array[Byte]]))
      .thenAnswer((i) => {
        val key = ByteBuffer.wrap(i.getArguments()(0).asInstanceOf[Array[Byte]])
        map.get(key) match {
          case Some(x) => x.array()
          case None => null
        }
      })

    Mockito.when(dbMock update(ArgumentMatchers.any(), ArgumentMatchers.any(classOf[Array[Byte]])))
      .thenAnswer((i) => {
        val upd = i.getArguments()(0).asInstanceOf[Array[Byte] => Array[Byte]]
        val key = ByteBuffer.wrap(i.getArguments()(1).asInstanceOf[Array[Byte]])
        val actual = map.get(key) match {
          case Some(x) => x.array()
          case None => null
        }
        val updated = upd(actual)
        map.put(key, ByteBuffer.wrap(updated))
      })

    storage = new StorageKeeper.PersistenceJockey(dbMock)
    exRateService = new ExchangeRateServiceImpl(storage)
    accountService = new AccountServiceImpl(storage, exRateService)
  }

  feature("account service") {
    scenario("set and get account successfully") {
      Given("init test account")
      val account = TestBase.rurAccount

      When("set-get-account")
      accountService.saveAccount(account)
      val res = accountService.getAccount(account.number)

      Then("compare result")
      assert(res == account)
    }

    scenario("single-currency-topup successfully") {
      Given("init test account")
      val target = TestBase.rurAccount
      val source = TestBase.rurAccount2
      val transactionRequest = Transaction(source.number, BigDecimal(20), Currency.rur())

      When("set-and-topup")
      accountService.saveAccount(target)
      accountService.saveAccount(source)
      accountService.topup(target.number, transactionRequest)
      val resCredit = accountService.getAccount(target.number)
      val resDibit = accountService.getAccount(source.number)

      Then("compare result")
      assert(resCredit.balance.amount == target.balance.amount + 20)
      assert(resDibit.balance.amount == source.balance.amount - 20)
    }

    scenario("single-currency-withdraw successfully") {
      Given("init test single-cur-accounts")
      val target = TestBase.rurAccount
      val source = TestBase.rurAccount2
      val transactionRequest = Transaction(source.number, BigDecimal(20), Currency.rur())

      When("set-and-withdraw")
      accountService.saveAccount(target)
      accountService.saveAccount(source)
      accountService.withdraw(target.number, transactionRequest)
      val resDebit = accountService.getAccount(target.number)
      val resCredit = accountService.getAccount(source.number)

      Then("compare result")
      assert(resDebit.balance.amount == target.balance.amount - 20)
      assert(resCredit.balance.amount == source.balance.amount + 20)
    }

    scenario("multi-currency-topup successfully") {
      Given("init test multi-cur-accounts")
      val rurToUsdRate = 0.01613
      val target = TestBase.rurAccount
      val source = TestBase.usdAccount
      val transactionRequest = Transaction(source.number, BigDecimal(20), Currency.rur())

      When("set-and-topup")
      exRateService.setupRates()
      accountService.saveAccount(target)
      accountService.saveAccount(source)
      accountService.topup(target.number, transactionRequest)
      val resCredit = accountService.getAccount(target.number)
      val resDibit = accountService.getAccount(source.number)

      Then("compare result")
      assert(resCredit.balance.amount == target.balance.amount + 20)
      assert(resDibit.balance.amount == source.balance.amount - (20 * rurToUsdRate))
    }

    scenario("not found account exception") {
      Given("init invalid transaction data")
      val transactionRequest = Transaction("123456", BigDecimal(20), Currency.rur())

      When("topup for unknown account")
      val caught =
        intercept[AccountNotFoundException] {
          accountService.topup("5432", transactionRequest)
        }

      Then("compare result")
      assert(caught.msg == "Credit account not found")
    }

    scenario("target account equals partner exception") {
      Given("init one-account transaction data")
      val transactionRequest = Transaction("123456", BigDecimal(20), Currency.rur())

      When("topup for unknown account")
      val caught =
        intercept[IncorrectTransactionException] {
          accountService.topup("123456", transactionRequest)
        }

      Then("compare result")
      assert(caught.msg == "Target account equals partner")
    }
  }
}
