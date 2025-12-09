package org.eazyportal.plugin.common.integration.test.testcase

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

class Given<T : BaseProjectTestCase>(
    private val testCase: T,
    initProjectBlock: GradleProjectBuilder.() -> Unit,
) {

    init {
        testCase.initializeProject(initProjectBlock)
    }

    fun givenConfiguration(projectConfigurationBlock: T.() -> Unit): Given<T> =
        also { projectConfigurationBlock(testCase) }

    fun whenGradleTask(
        taskName: String,
        vararg arguments: String,
        gradleTaskBlock: GradleRunner.() -> BuildResult,
    ): When<T> =
        createGradleRunner(testCase.projectDir, taskName, *arguments)
            .let(gradleTaskBlock)
            .let { When(testCase, it) }

    fun whenGradleTaskFails(
        taskName: String,
        vararg arguments: String,
    ): When<T> =
        whenGradleTask(taskName, *arguments) {
            buildAndFail()
        }

    fun whenGradleTaskSucceeds(
        taskName: String,
        vararg arguments: String,
    ): When<T> =
        whenGradleTask(taskName, *arguments) {
            build()
        }

}

class When<T : BaseProjectTestCase>(
    private val testCase: T,
    private val result: BuildResult,
) {

    fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): Then<T> =
        Then(testCase, result)
            .also { it.thenAssertTaskOutput(block) }

}

class Then<T : BaseProjectTestCase>(
    private val testCase: T,
    private val buildResult: BuildResult,
) {

    fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): Then<T> =
        also { block(assertThat(buildResult.output.lines())) }

    @Suppress("UNCHECKED_CAST")
    fun <A: Assert<in T>> thenAssert(block: A.() -> Unit) {
        block(Assert(testCase) as A)
    }

    open class Assert<T: BaseProjectTestCase>(
        private val testCase: T
    ) {

    }

}

