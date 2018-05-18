package com.revolut.testproject

import org.scalatra.{BadRequest, InternalServerError, Ok}

object Response {
  val headers = Map("Content-Type" -> "application/json",
    "Accept" -> "application/json")

  val Okey = () => Ok("", headers)
  val Success = (json: String) => Ok(json, headers)
  val BadRq = (json: String) => BadRequest(json, headers)
  val GeneralError = () => InternalServerError("", headers)
}
