package org.eazyportal.plugin.gradle.release.asd

import org.junit.jupiter.api.Test

object DSL3 {

    //------------------------------------
    // DSL
    //------------------------------------

    @DslMarker
    annotation class TestDsl

    @TestDsl
    class GivenScope<T : GivenContext>(private val context: T) {
        fun givenTestCase(block: T.() -> Unit) {
            context.block()
        }
    }

    @TestDsl
    class WhenScope<T : WhenContext>(private val context: T) {
        fun whenExecute(block: T.() -> Unit) {
            context.block()
        }
    }

    @TestDsl
    class ThenScope<T : ThenContext>(private val context: T) {
        fun thenValidate(block: T.() -> Unit) {
            context.block()
        }
    }

    @TestDsl
    class TestCaseDsl(
        context: ScmProjectTestContext
    ) {
        private val given = GivenScope(context)
        private val whenStage = WhenScope(context)
        private val then = ThenScope(context)

        fun givenTestCase(block: ScmProjectTestContext.() -> Unit) =
            given.givenTestCase(block)

        fun whenExecute(block: ScmProjectTestContext.() -> Unit) =
            whenStage.whenExecute(block)

        fun thenValidate(block: ScmProjectTestContext.() -> Unit) =
            then.thenValidate(block)
    }

    //------------------------------------
    // Context
    //------------------------------------

    interface GivenContext
    interface WhenContext
    interface ThenContext

    interface GradleProjectGivenContext : GivenContext {
        fun withGradleProject(block: GradleProjectBuilder.() -> Unit)
    }

    interface ScmProjectGivenContext : GivenContext {
        fun withScmProject(block: ScmProjectBuilder.() -> Unit)
    }

    interface ExecutionWhenContext : WhenContext {
        fun whenExecute()
    }

    interface OutputThenContext : ThenContext {
        fun assertOutput(block: OutputAssert.() -> Unit)
    }

    interface TestContext

    //------------------------------------
    // Helpers
    //------------------------------------
    class GradleProjectBuilder {
        fun setVersion(version: String) {
            println("[HERE] [GradleProjectBuilder] set version: $version")
        }
    }

    class ScmProjectBuilder {
        fun setBranch(branch: String) {
            println("[HERE] [ScmProjectBuilder] set branch: $branch")
        }
    }

    class OutputAssert {
        fun contains(text: String) {
            println("[HERE] [OutputAssert] contains text: $text")
        }
    }



    class ScmProjectTestContext :
        GradleProjectGivenContext,
        ScmProjectGivenContext,
        ExecutionWhenContext,
        OutputThenContext,
        TestContext {

        override fun withGradleProject(block: GradleProjectBuilder.() -> Unit) {
            GradleProjectBuilder().apply(block)
        }

        override fun withScmProject(block: ScmProjectBuilder.() -> Unit) {
            ScmProjectBuilder().apply(block)
        }

        override fun whenExecute() {
            println("Executing test")
        }

        override fun assertOutput(block: OutputAssert.() -> Unit) {
            OutputAssert().apply(block)
        }
    }

    //------------------------------------
    // Test case
    //------------------------------------

    class TestCase(
        private val context: ScmProjectTestContext
    ) {
        fun run(block: TestCaseDsl.() -> Unit) {
            TestCaseDsl(context).block()
        }
    }

    fun runTest(block: TestCaseDsl.() -> Unit) {
        val context = ScmProjectTestContext()
        TestCase(context).run(block)
    }

}

class DSL3Test {
    @Test
    fun test() = DSL3.runTest {
        givenTestCase {
            withGradleProject {
                setVersion("1.0.0")
            }
            withScmProject {
                setBranch("main")
            }
        }

        whenExecute {
            whenExecute()
        }

        thenValidate {
            assertOutput {
                contains("BUILD SUCCESS")
            }
        }
    }

}