package com.revolut.test

import com.revolut.intro.ScalaIntro
import spock.lang.Specification

class ScalaIntroTest extends Specification {

    def "scala simple logic test"() {
        when:
        def res = new ScalaIntro().hello()

        then:
        res == "Hello World"
    }

    def "sum must be correctly"() {
        when:
        def res = new ScalaIntro().sum(5, 3)

        then:
        res == 8
    }

    def "callback returns correct difference"() {
        when:
        def res = new ScalaIntro().callbackFor({ a, b -> a - b }, 8, 5)

        then:
        res == 3
    }
}
