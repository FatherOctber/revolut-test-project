package com.revolut.testproject.domain

import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}

object Domain {
  val domainPrefix = "accounts:"

  abstract class DomainModel

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  case class Account(@JsonProperty(required = true) number: String, @JsonProperty(required = true) client: Client, balance: Balance, status: String) extends DomainModel

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  case class Client(@JsonProperty(required = true) name: String)(implicit address: Address) extends DomainModel

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  case class Address(country: String, city: String, street: String, index: Long) extends DomainModel

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  case class Balance(amount: BigDecimal, currency: String) extends DomainModel

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  case class Transaction(@JsonProperty(required = true) partnerAccount: String, @JsonProperty(required = true) amount: BigDecimal, @JsonProperty(required = true) currency: String) extends DomainModel

  object Status {
    val statuses = Set("Active", "Closed")

    def check(status: String): Boolean = statuses.contains(status)

    def active() = statuses.head

    def closed() = statuses.tail
  }

  object Currency {
    val currencies = Set("RUR", "USD")

    def check(currency: String): Boolean = currencies.contains(currency)

    def rur() = currencies.head

    def usd() = currencies.tail
  }

}
