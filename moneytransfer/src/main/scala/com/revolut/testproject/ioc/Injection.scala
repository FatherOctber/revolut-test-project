package com.revolut.testproject.ioc

import java.time.Duration

import com.google.inject.{AbstractModule, Guice, TypeLiteral}
import com.revolut.testproject.dbadapter.{Persistence, RedisAdapter}
import redis.clients.jedis.{JedisPool, JedisPoolConfig}

object Injection {

  class RedisPersistenceModule extends AbstractModule {
    override def configure(): Unit = {
      val jedisPool = new JedisPool(buildPoolConfig(), "localhost")
      val redisAdapter = new RedisAdapter(jedisPool)
      bind(new TypeLiteral[Persistence[Array[Byte], Array[Byte]]] {}).toInstance(redisAdapter)
    }

    private def buildPoolConfig(): JedisPoolConfig = {
      val poolConfig = new JedisPoolConfig()
      poolConfig.setMaxTotal(128)
      poolConfig.setMaxIdle(128)
      poolConfig.setMinIdle(16)
      poolConfig.setTestOnBorrow(true)
      poolConfig.setTestOnReturn(true)
      poolConfig.setTestWhileIdle(true)
      poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis())
      poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis())
      poolConfig.setNumTestsPerEvictionRun(3)
      poolConfig.setBlockWhenExhausted(true)
      poolConfig
    }
  }

  def injector = Guice.createInjector(new RedisPersistenceModule)
}
