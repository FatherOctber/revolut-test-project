package com.fatheroctober.moneytransfer.ioc

import java.time.Duration

import com.fatheroctober.dbadapter.{Persistence, RedisAdapter}
import com.fatheroctober.moneytransfer.domain.{AccountService, AccountServiceImpl, ExchangeRateService, ExchangeRateServiceImpl}
import com.google.inject.{AbstractModule, Guice, Singleton, TypeLiteral}
import org.slf4j.LoggerFactory
import redis.clients.jedis.{JedisPool, JedisPoolConfig}

object Injection {

  @Singleton
  case class RedisPoolFuel(port: Int) extends ShutdownResource {
    val logger = LoggerFactory.getLogger(getClass)
    val redisPool = new JedisPool(buildPoolConfig(), "localhost", port)

    override def shutdown(): Unit = {
      logger.info("Shutdown redis connection pool")
      redisPool.close()
      redisPool.destroy()
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


  class MoneyTransferModule(redisPort: Int = 6379) extends AbstractModule {
    override def configure(): Unit = {
      val shutdownResource = RedisPoolFuel(redisPort)
      val jedisPool = shutdownResource.redisPool
      val redisAdapter = new RedisAdapter(jedisPool)
      bind(new TypeLiteral[ShutdownResource] {}).toInstance(shutdownResource)
      bind(new TypeLiteral[Persistence[Array[Byte], Array[Byte]]] {}).toInstance(redisAdapter)
      bind(new TypeLiteral[AccountService] {}).to(classOf[AccountServiceImpl])
      bind(new TypeLiteral[ExchangeRateService] {}).to(classOf[ExchangeRateServiceImpl])
    }
  }

  def injector(dbPort: Int) = Guice.createInjector(new MoneyTransferModule(dbPort))

  def injector = Guice.createInjector(new MoneyTransferModule())
}
