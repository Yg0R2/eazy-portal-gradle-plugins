package org.eazyportal.plugin.gradle.release.asd

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

object DSL2 {

    interface Given
    interface When
    interface Then

    class TestScenario<G : Given, W : When, T : Then>(
        private val givenFactory: () -> G,
        private val whenFactory: () -> W,
        private val thenFactory: () -> T,
    ) {
        fun givenScenario(block: G.() -> Unit) {
            givenFactory().block()
        }

        fun whenExecute(block: W.() -> Unit) {
            whenFactory().block()
        }

        fun thenValidate(block: T.() -> Unit) {
            thenFactory().block()
        }
    }

    interface TestCase<G : Given, W : When, T : Then> {
        fun init()

        fun runTestCase(block: TestScenario<G, W, T>.() -> Unit)
    }

    // -------------------------------------------------------------
    // Base implementation
    // -------------------------------------------------------------
    class GradleProjectGiven : Given
    class GradleProjectWhen : When
    class GradleProjectThen : Then

    open class BaseGradleTestCase : TestCase<GradleProjectGiven, GradleProjectWhen, GradleProjectThen> {
        @BeforeEach
        override fun init() {
            println("[HERE] [init] ${this::class.java.simpleName}")
        }

        override fun runTestCase(
            block: TestScenario<GradleProjectGiven, GradleProjectWhen, GradleProjectThen>.() -> Unit
        ) {
            println("[HERE] [runTestCase] ${this::class.java.simpleName}")

            TestScenario(
                givenFactory = { GradleProjectGiven() },
                whenFactory = { GradleProjectWhen() },
                thenFactory = { GradleProjectThen() },
            ).block()
        }
    }

    // -------------------------------------------------------------
    // SCM implementation
    // -------------------------------------------------------------
    class ScmProjectGiven : Given
    class ScmProjectWhen : When
    class ScmProjectThen : Then

    abstract class BaseScmProjectTestCase : TestCase<ScmProjectGiven, ScmProjectWhen, ScmProjectThen> {
        override fun runTestCase(block: TestScenario<ScmProjectGiven, ScmProjectWhen, ScmProjectThen>.() -> Unit) {
            println("[HERE] [runTestCase] ${this::class.java.simpleName}")

            TestScenario(
                givenFactory = { ScmProjectGiven() },
                whenFactory = { ScmProjectWhen() },
                thenFactory = { ScmProjectThen() },
            ).block()
        }
    }

    abstract class BaseSingleModuleScmProjectTestCase : BaseScmProjectTestCase()

    open class BaseSingleModuleGitFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase() {
        @BeforeEach
        override fun init() {
            println("[HERE] [init] ${this::class.java.simpleName}")
        }
    }

    open class BaseSingleModuleTrunkFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase() {
        @BeforeEach
        override fun init() {
            println("[HERE] [init] ${this::class.java.simpleName}")
        }
    }

    open class BaseSingleModuleCustomizedScmProjectTestCase : BaseSingleModuleScmProjectTestCase() {
        @BeforeEach
        override fun init() {
            println("[HERE] [init] ${this::class.java.simpleName}")
        }
    }

    abstract class BaseMultiModuleScmProjectTestCase : BaseScmProjectTestCase()

    open class BaseMultiModuleGitFlowScmProjectTestCase : BaseMultiModuleScmProjectTestCase() {
        @BeforeEach
        override fun init() {
            println("[HERE] [init] ${this::class.java.simpleName}")
        }
    }

    open class BaseMultiModuleTrunkFlowScmProjectTestCase : BaseMultiModuleScmProjectTestCase() {
        @BeforeEach
        override fun init() {
            println("[HERE] [init] ${this::class.java.simpleName}")
        }
    }

    open class BaseMultiModuleCustomizedScmProjectTestCase : BaseMultiModuleScmProjectTestCase() {
        @BeforeEach
        override fun init() {
            println("[HERE] [init] ${this::class.java.simpleName}")
        }
    }

}

class DSL2Test {

    interface DSL2TestCase {

        fun `test execution`()

    }

    @Nested
    inner class BaseGradleDSL2Test : DSL2.BaseGradleTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class SingleModuleGitFlowDSL2ProjectTest : DSL2.BaseSingleModuleGitFlowScmProjectTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class SingleModuleTrunkFlowDSL2ProjectTest : DSL2.BaseSingleModuleTrunkFlowScmProjectTestCase(),
        DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class SingleModuleCustomizedDSL2ProjectTest : DSL2.BaseSingleModuleCustomizedScmProjectTestCase(),
        DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class MultiModuleGitFlowDSL2ProjectTest : DSL2.BaseMultiModuleGitFlowScmProjectTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class MultiModuleTrunkFlowDSL2ProjectTest : DSL2.BaseMultiModuleTrunkFlowScmProjectTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class MultiModuleCustomizedDSL2ProjectTest : DSL2.BaseMultiModuleCustomizedScmProjectTestCase(),
        DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

}
