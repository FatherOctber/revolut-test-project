package com.fatheroctober.moneytransfer.domain

trait ExchangeRateService {

  def setupRates(): Unit

  def convert(currencyFrom: String, currencyTo: String, amount: BigDecimal): BigDecimal
}
