package org.eazyportal.plugin.common.integration.test.testcase.builder

import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

object GradleProjectTestCaseBuilder {

    open class BaseProjectGiven<P : BaseGradleProjectTestCase>(
        private val testCase: P,
        private val initProjectBlock: GradleProjectBuilder.() -> Unit,
    ) : TestCaseBuilder.Given<P, BaseProjectGiven<P>>(
        testCase,
        initProjectBlock,
    ) {

        override fun whenGradleTask(
            taskName: String,
            vararg arguments: String,
            gradleTaskBlock: GradleRunner.() -> BuildResult,
        ): BaseProjectWhen<P> =
            createGradleRunner(testCase.projectDir, taskName, *arguments)
                .let(gradleTaskBlock)
                .let { BaseProjectWhen(testCase, it) }

    }

    open class BaseProjectWhen<P : BaseGradleProjectTestCase>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.When<P, BaseProjectWhen<P>>(
        testCase,
        buildResult,
    ) {

        override fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): BaseProjectThen<P> =
            BaseProjectThen(testCase, buildResult)
                .thenAssertTaskOutput(block)

    }

    open class BaseProjectThen<P : BaseGradleProjectTestCase>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.Then<P, BaseProjectThen<P>>(
        testCase,
        buildResult,
    )

}
