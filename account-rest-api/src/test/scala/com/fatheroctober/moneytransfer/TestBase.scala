package com.fatheroctober.moneytransfer

import com.fatheroctober.moneytransfer.domain.Domain._

object TestBase {
  val testClient1 = Client("Bob", Address("Eng", "London", "Covent Garden", 123456))
  val testClient2 = Client("Robert", Address("Eng", "London", "Covent Garden", 123456))

  val rurAccount = Account("test1", testClient1, Balance(BigDecimal(100), Currency.rur()), Status.active())
  val usdAccount = Account("test2", testClient2, Balance(BigDecimal(5), Currency.usd()), Status.active())
  val rurAccount2 = Account("test3", testClient1, Balance(BigDecimal(200), Currency.rur()), Status.active())
}
