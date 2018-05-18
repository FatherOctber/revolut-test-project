package com.revolut.testproject.dbadapter

import java.nio.ByteBuffer
import java.util.Collections

import com.revolut.testproject.dbadapter.utils.SerializationUtil
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.{Matchers, Mockito}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FeatureSpec, GivenWhenThen}
import redis.clients.jedis._

import scala.collection.mutable

class RedisAdapterTest extends FeatureSpec with GivenWhenThen with BeforeAndAfter
  with MockitoSugar {
  info("Redis DB Adapter unit tests")

  var connectionPoolMock: JedisPool = _
  var jedisMock: Jedis = _
  var transactionMock: Transaction = _
  var testRedisAdapter: RedisAdapter = _
  var map = new mutable.HashMap[ByteBuffer, ByteBuffer]()

  before {
    connectionPoolMock = mock[JedisPool]
    jedisMock = mock[Jedis]
    transactionMock = mock[Transaction]
    testRedisAdapter = new RedisAdapter(connectionPoolMock)

    Mockito.when(connectionPoolMock.getResource).thenReturn(jedisMock)
    Mockito.when(jedisMock.watch(Matchers.any(classOf[Array[Byte]]))).thenReturn("Ok")
    Mockito.when(jedisMock.unwatch).thenReturn("Ok")
    Mockito.when(jedisMock.multi).thenReturn(transactionMock)
    Mockito.when(transactionMock.set(Matchers.any(classOf[Array[Byte]]), Matchers.any(classOf[Array[Byte]])))
      .thenAnswer(answerSet[Response[String]]((_) => new Response[String](BuilderFactory.STRING)))

    Mockito.when(transactionMock.get(Matchers.any(classOf[Array[Byte]])))
      .thenAnswer(answerSet[Response[Array[Byte]]]((_) => new Response[Array[Byte]](BuilderFactory.BYTE_ARRAY)))

    Mockito.when(transactionMock.exec).thenReturn(Collections.emptyList[AnyRef])
    Mockito.when(jedisMock.set(Matchers.any(classOf[Array[Byte]]), Matchers.any(classOf[Array[Byte]]))).thenAnswer(answerSet[String](s => s))
    Mockito.when(jedisMock.get(Matchers.any(classOf[Array[Byte]]))).thenAnswer(answerGet[Array[Byte]](bytes => bytes))
  }

  feature("redis persistence") {
    scenario("set and get data successfully") {
      Given("data")
      val data = TestBase.SomeObj("test_o", 1000)
      val rawData = SerializationUtil.toJson(data)
      val rawKey = SerializationUtil.toJson("some/key")

      When("set-get")
      testRedisAdapter << (rawKey, rawData)
      val res = testRedisAdapter >> rawKey

      Then("expected")
      assert(res != null)
      val resObj = SerializationUtil.fromJson[TestBase.SomeObj](res)
      assert(data == resObj)
    }

    scenario("cas operation successfully") {
      Given("data")
      val old = SerializationUtil.toJson(TestBase.SomeObj("test_o", 0))
      val other = SerializationUtil.toJson(TestBase.SomeObj("test_o", 200))
      val key = SerializationUtil.toJson("some/key")

      When("cas")
      testRedisAdapter << (key, old)
      testRedisAdapter update((actual) => {
        val actualObj = SerializationUtil.fromJson[TestBase.SomeObj](actual)
        SerializationUtil.toJson(TestBase.SomeObj(actualObj.name, 200))
      }, key)

      Then("expected")
      compareWithStorage(key, other)
    }
  }

  def compareWithStorage(key: Array[Byte], expected: Array[Byte]): Unit = {
    val wrappedKey = ByteBuffer.wrap(key)
    val wrappedExpected = ByteBuffer.wrap(expected)
    val actual = map.get(wrappedKey).orNull

    assert(actual == wrappedExpected)
  }

  def answerSet[T](resConversion: String => T) = new Answer[T] {
    override def answer(i: InvocationOnMock): T = {
      var args = i.getArguments
      var key = args(0) match {
        case x: Array[Byte] => ByteBuffer.wrap(x)
        case _ => null
      }
      var value = args(1) match {
        case x: Array[Byte] => ByteBuffer.wrap(x)
        case _ => null
      }
      map.put(key, value)
      resConversion("Ok")
    }
  }

  def answerGet[T](resConversion: Array[Byte] => T) = new Answer[T] {
    override def answer(i: InvocationOnMock): T = {
      var args = i.getArguments
      var key = args(0) match {
        case x: Array[Byte] => ByteBuffer.wrap(x)
        case _ => null
      }
      var raw = map.get(key).orNull
      var data = if (raw != null) raw.array() else null
      resConversion(data)
    }
  }
}
