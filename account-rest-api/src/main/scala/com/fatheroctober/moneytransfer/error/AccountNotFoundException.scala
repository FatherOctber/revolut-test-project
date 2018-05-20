package com.fatheroctober.moneytransfer.error

case class AccountNotFoundException(msg: String) extends RuntimeException(msg)