package com.fatheroctober.moneytransfer

import org.scalatra.{BadRequest, InternalServerError, NotFound, Ok}

object Response {
  val headers = Map("Content-Type" -> "application/json",
    "Accept" -> "application/json")

  val Okay = () => Ok("", headers)
  val Success = (body: Any) => Ok(body, headers)
  val BadRq = (body: Any) => BadRequest(body, headers)
  val GeneralError = () => InternalServerError("", headers)
  val RequestedNotFound = () => NotFound("", headers)
}
