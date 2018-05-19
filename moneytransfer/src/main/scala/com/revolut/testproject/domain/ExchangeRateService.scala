package com.revolut.testproject.domain

trait ExchangeRateService {

  def setupRates(): Unit

  def convert(currencyFrom: String, currencyTo: String, amount: BigDecimal): BigDecimal
}
