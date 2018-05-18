package com.revolut.testproject

import com.google.inject.Inject
import com.revolut.testproject.dbadapter.Persistence
import com.revolut.testproject.dbadapter.utils.SerializationUtil
import com.revolut.testproject.domain.Domain.{Account, DomainModel}

import scala.reflect._

object StorageKeeper {

  case class Key(body: String) {
    def raw(): Array[Byte] = disassemble(body)
  }

  class PersistenceJockey @Inject()(storage: Persistence[Array[Byte], Array[Byte]]) {

    val accountTag = classTag[Account]

    def keep[T <: DomainModel](model: T)(implicit key: Key): Unit = model match {
      case x: Account => storage << (key.raw(), disassemble(model))
      case _ => throw unsupportedType
    }

    def get[T <: DomainModel](key: Key)(implicit tag: ClassTag[T]): Account = tag match {
      case accountTag => assembleAccount(storage >> key.raw())
      case _ => throw unsupportedType
    }

    def update[T <: DomainModel](updateIt: (Array[Byte]) => Array[Byte])(implicit key: Key, tag: ClassTag[T]): Unit = tag match {
      case accountTag => storage update (updateIt, key.raw())
      case _ => throw unsupportedType
    }

    private def unsupportedType() = new RuntimeException("Unsupported model type")

  }

  def disassemble(model: AnyRef): Array[Byte] = SerializationUtil.toJson(model)

  def assembleAccount(raw: Array[Byte]): Account = SerializationUtil.fromJson[Account](raw)

}