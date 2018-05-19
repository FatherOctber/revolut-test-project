package com.revolut.testproject.dbadapter

import com.google.inject.{Inject, Singleton}
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.{Jedis, JedisPool}

import util.control.Breaks._

@Singleton
class RedisAdapter @Inject()(connectionPool: JedisPool) extends Persistence[Array[Byte], Array[Byte]] {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def <<(key: Array[Byte], data: Array[Byte]): Unit = {
    var jedis: Jedis = null
    try {
      logger.info("set data to Redis (" + key + ", " + data + "). . .")
      jedis = connectionPool.getResource
      jedis.set(key, data)
    } catch {
      case e: Exception => {
        logger.error("error set data to Redis:", e)
        throw e
      }
    } finally {
      if (jedis != null) jedis.close()
    }
  }

  override def >>(key: Array[Byte]): Array[Byte] = {
    var jedis: Jedis = null
    try {
      logger.info("get data from Redis (" + key + "). . .")
      jedis = connectionPool.getResource
      jedis.get(key)
    } catch {
      case e: Exception => {
        logger.error("error get data from Redis:", e)
        throw e
      }
    } finally {
      if (jedis != null) jedis.close()
    }
  }

  override def update(update: (Array[Byte]) => Array[Byte], key: Array[Byte]): Unit = {
    var jedis: Jedis = null
    try {
      logger.info("update data in Redis (" + key + "). . .")
      jedis = connectionPool.getResource
      breakable {
        while (true) {
          jedis.watch(key)
          var actual = jedis.get(key)
          val updated = update(actual)
          var transaction = jedis.multi()
          transaction.set(key, updated)
          if (transaction.exec() != null) {
            logger.info("successfully updated data in Redis (" + key + ")")
            jedis.unwatch()
            break
          }
        }
      }
    } catch {
      case e: Exception => throw e
    } finally {
      if (jedis != null) jedis.close()
    }

  }

}
