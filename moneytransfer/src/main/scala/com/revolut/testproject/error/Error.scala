package com.revolut.testproject.error

import com.revolut.testproject.dbadapter.utils.SerializationUtil
import com.revolut.testproject.Response


object Error {
  val serializationError = (99, "E_SERIALIZATION_ERROR")
  val transactionError = (100, "E_TRANSACTION_ERROR")

  val errorHandling: PartialFunction[Throwable, Any] = {
    case e: IncorrectTransactionException => Response.BadRq(ErrorResponse(transactionError._1, e.getMessage).toString)
    case e => Response.GeneralError()
  }

  case class ErrorResponse(code: Long, msg: String) {
    override def toString: String = new String(SerializationUtil.toJson(this), "UTF-8")
  }

}
