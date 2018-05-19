package com.revolut.testproject.domain

import com.google.inject.Inject
import com.revolut.testproject.StorageKeeper
import com.revolut.testproject.StorageKeeper.Key
import com.revolut.testproject.domain.Domain._
import org.slf4j.{Logger, LoggerFactory}

class ExchangeRateServiceImpl @Inject()(dbJockey: StorageKeeper.PersistenceJockey) extends ExchangeRateService {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def setupRates(): Unit = {
    logger.info("Setup exchange rate list...")
    val rates = List(
      ExchangeRate(Currency.rur(), Set(
        ConversionRate(Currency.usd(), BigDecimal(0.01613)),
        ConversionRate(Currency.rur(), BigDecimal(1))
      )),
      ExchangeRate(Currency.usd(), Set(
        ConversionRate(Currency.rur(), BigDecimal(62)),
        ConversionRate(Currency.usd(), BigDecimal(1))
      ))
    )
    rates.foreach(rate => {
      implicit val key = Key(Domain.exrateDomain + rate.baseCurrency)
      dbJockey.keep(rate)
    })
    logger.info("Setup of exchange rate list completed successfully")
  }

  override def convert(currencyFrom: String, currencyTo: String, amount: BigDecimal): BigDecimal = {
    logger.info("Convert " + currencyFrom + " to " + currencyTo)
    val key = Key(Domain.exrateDomain + currencyFrom)
    val rate = dbJockey.get[ExchangeRate](key)
    val rateValue = rate.conversionRates collectFirst { case i if i.toCurrency == currencyTo => i.rate }
    rateValue match {
      case Some(x) => amount * x
      case None => amount
    }
  }

}
