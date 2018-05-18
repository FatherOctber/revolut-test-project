package com.revolut.testproject.domain

import com.google.inject.Inject
import com.revolut.testproject.StorageKeeper
import com.revolut.testproject.StorageKeeper.Key
import com.revolut.testproject.domain.Domain._
import com.revolut.testproject.error.IncorrectTransactionException
import org.slf4j.{Logger, LoggerFactory}

case class AccountService @Inject()(dbJockey: StorageKeeper.PersistenceJockey) {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def saveAccount(account: Account): Unit = {
    implicit val key = Key(domainPrefix + account.number)
    val formattedAccount = formatAccount(account)
    dbJockey.keep(formattedAccount)
  }

  def getAccount(accountNUmber: String): Account = dbJockey.get[Domain.Account](Key(domainPrefix + accountNUmber))

  def topup(targetAccount: String, transaction: Transaction): Unit = {
    if (targetAccount == transaction.partnerAccount) throw new IncorrectTransactionException("Target account equals partner")

    val creditTarget = (updatedRaw: Array[Byte]) => {
      val actualAccount = StorageKeeper.assembleAccount(updatedRaw)
      val updAccount = updateBalance(actualAccount, transaction, isCredit = true)
      StorageKeeper.disassemble(updAccount)
    }

    val debitSource = (updatedRaw: Array[Byte]) => {
      val actualAccount = StorageKeeper.assembleAccount(updatedRaw)
      val updAccount = updateBalance(actualAccount, transaction, isCredit = false)
      StorageKeeper.disassemble(updAccount)
    }
    val credit = () => {
      implicit val targetKey = Key(domainPrefix + targetAccount)
      dbJockey.update[Account](creditTarget)
    }
    credit()

    val debit = () => {
      implicit val partnerKey = Key(domainPrefix + transaction.partnerAccount)
      dbJockey.update[Account](debitSource)
    }
    debit()
  }

  def withdraw(sourceAccount: String, transaction: Transaction): Unit = {
    val withdrawalTransaction = Transaction(sourceAccount, transaction.amount, transaction.currency)
    val targetAccount = transaction.partnerAccount
    topup(targetAccount, withdrawalTransaction)
  }


  private def updateBalance(account: Account, transaction: Transaction, isCredit: Boolean): Account = {
    val actualBalance = Option(account).map(a => a.balance)
    val ready = (actualBalance) match {
      case Some(actual) => actual.amount > 0 && transaction.amount > 0 // overdraft is supported
      case _ => false
    }
    if (ready) {
      val updBalance = if (isCredit) {
        Balance(actualBalance.get.amount + transaction.amount, transaction.currency)
      } else {
        Balance(actualBalance.get.amount - transaction.amount, transaction.currency)
      }
      Account(account.number, account.client, updBalance, account.status)
    } else {
      logger.warn("Account " + account.number + " have not enough balance")
      account
    }

  }

  private def formatAccount(account: Account): Account = {
    val status = if (account.status == null) {
      Status.active
    } else if (Status.check(account.status)) account.status else throw new RuntimeException("Invalid status")

    val balance = checkedBalance(account.balance)

    Account(account.number, account.client, balance, status)
  }

  private def checkedBalance(balance: Balance): Balance = {
    Option(balance) match {
      case Some(bal) => if (Currency.check(bal.currency)) bal else throw new RuntimeException("Invalid currency")
      case None => Balance(BigDecimal(0), Currency.rur)
    }
  }
}