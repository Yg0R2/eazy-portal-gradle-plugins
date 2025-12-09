package org.eazyportal.plugin.common.integration.test.testcase.builder

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.TestCase
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

object TestCaseBuilder {

    abstract class Given<out P : TestCase<*>, SELF : Given<P, SELF>>(
        private val testCase: P,
        private val initProjectBlock: GradleProjectBuilder.() -> Unit,
    ) {

        init {
            testCase.initializeProject(initProjectBlock)
        }

        fun givenConfiguration(projectConfigurationBlock: P.() -> Unit): SELF {
            projectConfigurationBlock(testCase)

            @Suppress("UNCHECKED_CAST")
            return this as SELF
        }

        abstract fun whenGradleTask(
            taskName: String,
            vararg arguments: String,
            gradleTaskBlock: GradleRunner.() -> BuildResult,
        ): When<P, *>

        fun whenGradleTaskFails(
            taskName: String,
            vararg arguments: String,
        ): When<P, *> =
            whenGradleTask(taskName, *arguments) {
                buildAndFail()
            }

        fun whenGradleTaskSucceeds(
            taskName: String,
            vararg arguments: String,
        ): When<P, *> =
            whenGradleTask(taskName, *arguments) {
                build()
            }

    }

    abstract class When<out P : Any, SELF : When<P, SELF>>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) {

        abstract fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): Then<P, *>

    }

    abstract class Then<out P : Any, SELF : Then<P, SELF>>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) {

        fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): SELF {
            block(assertThat(buildResult.output.lines()))

            @Suppress("UNCHECKED_CAST")
            return this as SELF
        }

    }

}
