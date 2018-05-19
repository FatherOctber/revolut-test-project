package com.revolut.testproject.domain

import com.revolut.testproject.domain.Domain.{Account, Transaction}

trait AccountService {

  def saveAccount(account: Account): Unit

  def getAccount(accountNUmber: String): Account

  def topup(targetAccount: String, transaction: Transaction): Unit

  def withdraw(sourceAccount: String, transaction: Transaction): Unit
}
