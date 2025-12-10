package org.eazyportal.plugin.gradle.release.asd

import org.junit.jupiter.api.Nested


// -------------------------------------------------------------
// Base classes
// -------------------------------------------------------------
open class GivenDSL {
    fun given() {
        println("Given: given")
    }
}

open class WhenDSL {
    fun doSomething() {
        println("When: doSomething")
    }
}

open class ThenDSL {
    fun validate() {
        println("Then: validate")
    }
}

class BaseTestCase : TestCase<GivenDSL, WhenDSL, ThenDSL> {

    override fun runTest(block: TestScenarioDSL<GivenDSL, WhenDSL, ThenDSL>.() -> Unit) {
        TestScenarioDSL(
            { GivenDSL() },
            { WhenDSL() },
            { ThenDSL() },
        ).block()
    }

}

// -------------------------------------------------------------
// SCM classes
// -------------------------------------------------------------
open class ScmGivenDSL : GivenDSL()

open class ScmWhenDSL : WhenDSL() {
    fun doSomethingAnd(block: () -> Unit) {
        block()
        println("When: doSomethingAnd")
    }
}

open class ScmThenDSL : ThenDSL() {
    fun validateAnd(block: () -> Unit) {
        block()
        println("Then: validateAnd")
    }
}

class ScmTestCase : TestCase<ScmGivenDSL, ScmWhenDSL, ScmThenDSL> {
    override fun runTest(block: TestScenarioDSL<ScmGivenDSL, ScmWhenDSL, ScmThenDSL>.() -> Unit) {
        TestScenarioDSL(
            { ScmGivenDSL() },
            { ScmWhenDSL() },
            { ScmThenDSL() }
        ).block()
    }
}

// -------------------------------------------------------------
// Test case and scenario
// -------------------------------------------------------------
interface TestCase<G : GivenDSL, W : WhenDSL, T : ThenDSL> {
    fun runTest(block: TestScenarioDSL<G, W, T>.() -> Unit)
}

class TestScenarioDSL<G : GivenDSL, W : WhenDSL, T : ThenDSL>(
    private val givenFactory: () -> G = { GivenDSL() as G },
    private val whenFactory: () -> W = { WhenDSL() as W },
    private val thenFactory: () -> T = { ThenDSL() as T },
) {

    fun given(block: G.() -> Unit) {
        givenFactory().block()
    }

    fun whenDoing(block: W.() -> Unit) {
        whenFactory().block()
    }

    fun then(block: T.() -> Unit) {
        thenFactory().block()
    }

}

fun main() {
    ScmTestCase().runTest {
        given {
            given()
        }
        whenDoing {
            doSomething()
            doSomethingAnd {
                println("When: doSomethingAnd #2")
            }
        }
        then {
            validate()
            validateAnd {
                println("Then: validateAnd #2")
            }
        }
    }
}
