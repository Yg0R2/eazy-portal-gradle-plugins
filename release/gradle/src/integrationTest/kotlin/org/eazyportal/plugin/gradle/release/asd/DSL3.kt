package org.eazyportal.plugin.gradle.release.asd

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

object DSL3 {

    //------------------------------------
    // DSL
    //------------------------------------

    @DslMarker
    annotation class TestDsl

    @TestDsl
    class GivenScope<C : GivenContext>(private val context: C) {
        fun givenTestCase(block: C.() -> Unit) {
            context.block()
        }
    }

    @TestDsl
    class WhenScope<C : WhenContext>(private val context: C) {
        fun whenExecute(block: C.() -> Unit) {
            context.block()
        }
    }

    @TestDsl
    class ThenScope<C : ThenContext>(private val context: C) {
        fun thenValidate(block: C.() -> Unit) {
            context.block()
        }
    }

    @TestDsl
    class TestScenario<G : GivenContext, W : WhenContext, T : ThenContext>(
        givenContext: G,
        whenContext: W,
        thenContext: T,
    ) {
        private val given = GivenScope(givenContext)
        private val whenStage = WhenScope(whenContext)
        private val then = ThenScope(thenContext)

        fun givenTestCase(block: G.() -> Unit) =
            given.givenTestCase(block)

        fun whenExecute(block: W.() -> Unit) =
            whenStage.whenExecute(block)

        fun thenValidate(block: T.() -> Unit) =
            then.thenValidate(block)
    }

    //------------------------------------
    // Context
    //------------------------------------

    @TestDsl
    interface GivenContext
    @TestDsl
    interface WhenContext
    @TestDsl
    interface ThenContext

    open class GradleProjectGivenContext(
        private val gradleProjectBuilder: GradleProjectBuilder,
    ) : GivenContext {
        fun withGradleProject(block: GradleProjectBuilder.() -> Unit) {
            gradleProjectBuilder.block()
        }
    }

    open class ScmProjectGivenContext(
        private val gradleProjectBuilder: GradleProjectBuilder,
        private val scmProjectBuilder: ScmProjectBuilder,
    ) : GivenContext, GradleProjectGivenContext(gradleProjectBuilder) {
        fun withScmProject(block: ScmProjectBuilder.() -> Unit) {
            scmProjectBuilder.apply(block)
                .build()
        }
    }

    open class ExecutionWhenContext : WhenContext {
        fun execute() {
            println("Executing test")
        }
    }

    open class OutputThenContext : ThenContext {
        fun assertOutput(block: OutputAssert.() -> Unit) {
            OutputAssert().block()
        }
    }

    //------------------------------------
    // Helpers
    //------------------------------------
    @TestDsl
    class GradleProjectBuilder {
        fun setVersion(version: String) {
            println("[HERE] [GradleProjectBuilder] set version: $version")
        }

        fun build() {
            println("[HERE] [GradleProjectBuilder] build")
        }
    }

    @TestDsl
    class ScmProjectBuilder(
        private val gradleProjectBuilder: GradleProjectBuilder,
    ) {
        fun setBranch(branch: String) {
            println("[HERE] [ScmProjectBuilder] set branch: $branch")
        }

        fun build() {
            gradleProjectBuilder.build()
            println("[HERE] [ScmProjectBuilder] build")
        }
    }

    @TestDsl
    class OutputAssert {
        fun contains(text: String) {
            println("[HERE] [OutputAssert] contains text: $text")
        }
    }


    //------------------------------------
    // Test case
    //------------------------------------

    abstract class TestCase<G : GivenContext, W : WhenContext, T : ThenContext> {

        @IntegrationTestDsl
        fun runTest(block: TestScenario<G, W, T>.() -> Unit) {
            TestScenario(
                initGivenContext(),
                initWhenContext(),
                initThenContext(),
            ).block()
        }

        protected abstract fun initGivenContext(): G
        protected abstract fun initWhenContext(): W
        protected abstract fun initThenContext(): T
    }

    abstract class SingleModuleScmProjectTestCase :
        TestCase<ScmProjectGivenContext, ExecutionWhenContext, OutputThenContext>() {
        override fun initGivenContext(): ScmProjectGivenContext {
            val gradleProjectBuilder = GradleProjectBuilder()
            val scmProjectBuilder = ScmProjectBuilder(gradleProjectBuilder)

            return ScmProjectGivenContext(gradleProjectBuilder, scmProjectBuilder)
        }

        override fun initWhenContext(): ExecutionWhenContext =
            ExecutionWhenContext()

        override fun initThenContext(): OutputThenContext =
            OutputThenContext()
    }

}

class DSL3Test {

    interface DSL3TestCase {
        fun test()
    }

    @Nested
    inner class SimpleDSL3ProjectTest : DSL3TestCase, DSL3.SingleModuleScmProjectTestCase() {

        @Test
        override fun test() = runTest {
            givenTestCase {
                withGradleProject {
                    setVersion("1.0.0")
                }
                withScmProject {
                    setBranch("main")
                }
            }

            whenExecute {
                execute()
            }

            thenValidate {
                assertOutput {
                    contains("BUILD SUCCESS")
                }
            }
        }

    }

}
