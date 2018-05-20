package com.fatheroctober.moneytransfer.error

case class IncorrectTransactionException(msg: String) extends RuntimeException(msg)
