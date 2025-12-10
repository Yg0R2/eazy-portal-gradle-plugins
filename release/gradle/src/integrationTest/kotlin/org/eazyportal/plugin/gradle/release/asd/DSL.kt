package org.eazyportal.plugin.gradle.release.asd

object DSL {

    // -------------------------------------------------------------
    // Base Test Case (shared by all test-case types)
    // -------------------------------------------------------------
    interface BaseTestCase {
        fun runTest(block: TestScenarioDSL.() -> Unit)
    }

    // -------------------------------------------------------------
    // Main DSL containers
    // -------------------------------------------------------------
    class TestScenarioDSL(
        private val thenFactory: () -> BaseThenDSL
    ) {

        fun given(block: GivenDSL.() -> Unit) {
            GivenDSL().block()
        }

        fun whenDoing(block: WhenDSL.() -> Unit) {
            val whenDsl = WhenDSL(thenFactory)
            whenDsl.block()
        }

        fun then(block: BaseThenDSL.() -> Unit) {
            thenFactory().block()
        }
    }

    // -------------------------------------------------------------
    // G I V E N
    // -------------------------------------------------------------
    class GivenDSL {
        fun something() = println("Given: something")
    }

    // -------------------------------------------------------------
    // W H E N
    // -------------------------------------------------------------
    class WhenDSL(
        private val thenFactory: () -> BaseThenDSL
    ) {
        fun doSomething() {
            println("When: doing something")
        }

        fun doSomethingElse(block: () -> Unit) {
            block()
            println("When: did something else")
        }

        fun then(block: BaseThenDSL.() -> Unit) {
            thenFactory().block()
        }
    }

    // -------------------------------------------------------------
    // T H E N  (base)
    // -------------------------------------------------------------
    open class BaseThenDSL {
        open fun validate() {
            println("Then: validate")
        }
    }

    // -------------------------------------------------------------
    // SCM-Specific extensions
    // -------------------------------------------------------------
    class ScmThenDSL : BaseThenDSL() {

        override fun validate() {
            println("SCM validation")
        }

        fun asd() {
            println("[SCM] asd")
        }
    }

    class ScmTestCase : BaseTestCase {
        override fun runTest(block: TestScenarioDSL.() -> Unit) {
            TestScenarioDSL(
                thenFactory = { ScmThenDSL() }).block()
        }
    }

}


fun main() {
    DSL.ScmTestCase().runTest {
        given {
            println("setup SCM")
        }
        whenDoing {
            doSomething()
            doSomethingElse { println("additional SCM behavior") }
        }
        then {
            validate()
            asd()
        }
    }
}
