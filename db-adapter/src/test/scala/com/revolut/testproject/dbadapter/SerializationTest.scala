package com.revolut.testproject.dbadapter

import com.revolut.testproject.dbadapter.utils.SerializationUtil
import org.scalatest.{BeforeAndAfter, FeatureSpec, GivenWhenThen}
import org.scalatest.mockito.MockitoSugar

class SerializationTest extends FeatureSpec with GivenWhenThen with BeforeAndAfter
  with MockitoSugar {
  info("Serialization Util unit tests")

  feature("serialization feature") {
    scenario("serialize successfully") {
      Given("serialize-data")
      val testObj =  TestBase.SomeObj("Test",21)

      When("serialization")
      val res = SerializationUtil.toJson(testObj)

      Then("serialize-result")
      assert(res != null)
      assert(res.isInstanceOf[Array[Byte]])
    }

    scenario("deserialize successfully") {
      Given("deserialize-data")
      val testObj = TestBase.SomeObj("Test",21)

      When("deserialization")
      val bytes = SerializationUtil.toJson(testObj)
      val res = SerializationUtil.fromJson[TestBase.SomeObj](bytes)

      Then("deserialize-result")
      assert(res != null)
      assert(res.name == "Test")
      assert(res.value == 21)
    }
  }


}
