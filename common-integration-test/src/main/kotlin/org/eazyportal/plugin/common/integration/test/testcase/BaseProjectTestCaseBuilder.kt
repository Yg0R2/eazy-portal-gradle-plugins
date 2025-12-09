package org.eazyportal.plugin.common.integration.test.testcase

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

open class BaseProjectGiven<P : BaseProjectTestCase, out W: BaseProjectWhen<P>>(
    private val testCase: P,
    initProjectBlock: GradleProjectBuilder.() -> Unit,
) {

    init {
        testCase.initializeProject(initProjectBlock)
    }

    fun givenConfiguration(projectConfigurationBlock: P.() -> Unit): BaseProjectGiven<P, W> =
        also { projectConfigurationBlock(testCase) }

    open fun whenGradleTask(
        taskName: String,
        vararg arguments: String,
        gradleTaskBlock: GradleRunner.() -> BuildResult,
    ): BaseProjectWhen<P> =
        createGradleRunner(testCase.projectDir, taskName, *arguments)
            .let(gradleTaskBlock)
            .let { BaseProjectWhen(testCase, it) }

    fun whenGradleTaskFails(
        taskName: String,
        vararg arguments: String,
    ): BaseProjectWhen<P> =
        whenGradleTask(taskName, *arguments) {
            buildAndFail()
        }

    fun whenGradleTaskSucceeds(
        taskName: String,
        vararg arguments: String,
    ): BaseProjectWhen<P> =
        whenGradleTask(taskName, *arguments) {
            build()
        }

}

open class BaseProjectWhen<P : BaseProjectTestCase>(
    private val testCase: P,
    private val result: BuildResult,
) {

    open fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): BaseProjectThen<P> =
        BaseProjectThen(testCase, result)
            .also { it.thenAssertTaskOutput(block) }

}

open class BaseProjectThen<T : BaseProjectTestCase>(
    private val testCase: T,
    private val buildResult: BuildResult,
) {

    fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): BaseProjectThen<T> =
        also { block(assertThat(buildResult.output.lines())) }

}