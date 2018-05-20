package com.fatheroctober.moneytransfer.domain

import com.fatheroctober.moneytransfer.StorageKeeper
import com.fatheroctober.moneytransfer.StorageKeeper.Key
import com.fatheroctober.moneytransfer.domain.Domain._
import com.fatheroctober.moneytransfer.error.IncorrectTransactionException
import com.google.inject.Inject
import org.slf4j.{Logger, LoggerFactory}

case class AccountServiceImpl @Inject()(dbJockey: StorageKeeper.PersistenceJockey,
                                        exRateService: ExchangeRateService) extends AccountService {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def saveAccount(account: Account): Unit = {
    implicit val key = Key(accountDomain + account.number)
    val formattedAccount = formatAccount(account)
    dbJockey.keep(formattedAccount)
  }

  override def getAccount(accountNUmber: String): Account = dbJockey.get[Domain.Account](Key(accountDomain + accountNUmber))

  override def topup(targetAccount: String, transaction: Transaction): Unit = {
    if (targetAccount == transaction.partnerAccount) throw new IncorrectTransactionException("Target account equals partner")

    val creditTarget = (actualRaw: Array[Byte]) => {
      val actualAccount = StorageKeeper.assembleAccount(actualRaw)
      val updAccount = updateBalance(actualAccount, transaction, isCredit = true)
      StorageKeeper.disassemble(updAccount)
    }

    val debitSource = (actualRaw: Array[Byte]) => {
      val actualAccount = StorageKeeper.assembleAccount(actualRaw)
      val updAccount = updateBalance(actualAccount, transaction, isCredit = false)
      StorageKeeper.disassemble(updAccount)
    }
    val credit = () => {
      implicit val targetKey = Key(accountDomain + targetAccount)
      dbJockey.update[Account](creditTarget)
    }
    credit()

    val debit = () => {
      implicit val partnerKey = Key(accountDomain + transaction.partnerAccount)
      dbJockey.update[Account](debitSource)
    }
    debit()
  }

  override def withdraw(sourceAccount: String, transaction: Transaction): Unit = {
    val withdrawalTransaction = Transaction(sourceAccount, transaction.amount, transaction.currency)
    val targetAccount = transaction.partnerAccount
    topup(targetAccount, withdrawalTransaction)
  }


  private def updateBalance(account: Account, transaction: Transaction, isCredit: Boolean): Account = {
    val actualBalance = Option(account).map(a => a.balance)
    val ready = (actualBalance) match {
      case Some(actual) => actual.amount > 0 && transaction.amount > 0 // overdraft is available
      case _ => false
    }
    if (ready) {
      val ratedTransaction = exchangeRate(actualBalance.get, transaction)
      val updBalance = if (isCredit) {
        Balance(actualBalance.get.amount + ratedTransaction.amount, ratedTransaction.currency)
      } else {
        Balance(actualBalance.get.amount - ratedTransaction.amount, ratedTransaction.currency)
      }
      Account(account.number, account.client, updBalance, account.status)
    } else {
      logger.warn("Account " + account.number + " have not enough balance")
      account
    }
  }

  private def exchangeRate(balance: Balance, transaction: Transaction): Transaction = {
    if (balance.currency != transaction.currency) {
      val convertedAmount = exRateService.convert(transaction.currency, balance.currency, transaction.amount)
      Transaction(transaction.partnerAccount, convertedAmount, balance.currency)
    } else transaction
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