package com.revolut.testproject.dbadapter.utils

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object SerializationUtil {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)

  def toJson(value: Any): Array[Byte] = {
    mapper.writeValueAsBytes(value)
  }

  def fromJson[T](json: Array[Byte])(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }

}
