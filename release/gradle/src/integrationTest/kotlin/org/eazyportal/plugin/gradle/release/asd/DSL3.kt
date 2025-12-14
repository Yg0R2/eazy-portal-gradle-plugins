package org.eazyportal.plugin.gradle.release.asd

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

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
    class TestScenario<C : TestContext<*>>(
        context: C,
    ) {
        private val given = GivenScope(context)
        private val whenStage = WhenScope(context)
        private val then = ThenScope(context)

        fun givenTestCase(block: C.() -> Unit) =
            given.givenTestCase(block)

        fun whenExecute(block: C.() -> Unit) =
            whenStage.whenExecute(block)

        fun thenValidate(block: C.() -> Unit) =
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

    interface TestContext<SELF : TestContext<SELF>> : GivenContext, WhenContext, ThenContext {
        fun runTest(block: TestScenario<SELF>.() -> Unit) {
            TestCase(this as SELF).run(block)
        }
    }

    //------------------------------------
    // Helpers
    //------------------------------------
    class GradleProjectBuilder {
        fun setVersion(version: String) {
            println("[HERE] [GradleProjectBuilder] set version: $version")
        }

        fun build() {
            println("[HERE] [GradleProjectBuilder] build")
        }
    }

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

    class OutputAssert {
        fun contains(text: String) {
            println("[HERE] [OutputAssert] contains text: $text")
        }
    }

    abstract class ScmProjectTestContext :
        GradleProjectGivenContext,
        ScmProjectGivenContext,
        ExecutionWhenContext,
        OutputThenContext,
        TestContext<ScmProjectTestContext> {

        private val gradleProjectBuilder = GradleProjectBuilder()
        private val scmProjectBuilder = ScmProjectBuilder(gradleProjectBuilder)

        override fun withGradleProject(block: GradleProjectBuilder.() -> Unit) {
            gradleProjectBuilder.block()
        }

        override fun withScmProject(block: ScmProjectBuilder.() -> Unit) {
            scmProjectBuilder.apply(block)
                .build()
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

    class TestCase<C : TestContext<*>>(
        private val context: C
    ) {
        fun run(block: TestScenario<C>.() -> Unit) {
            TestScenario(context).block()
        }
    }

//    inline fun <reified C : TestContext<*>> runTest(noinline block: TestScenario<C>.() -> Unit) {
//        val context = C::class.createInstance()
//        TestCase(context).run(block)
//    }

}

class DSL3Test {

    interface DSL3TestCase {
        fun test()
    }

    @Nested
    inner class SimpleDSL3ProjectTest : DSL3TestCase, DSL3.ScmProjectTestContext() {

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
                whenExecute()
            }

            thenValidate {
                assertOutput {
                    contains("BUILD SUCCESS")
                }
            }
        }

    }


}