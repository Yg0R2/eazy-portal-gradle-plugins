package org.eazyportal.plugin.common.integration.test.testcase.builder

import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

object GradleProjectTestCaseBuilder {

    open class BaseProjectGiven<P : BaseGradleProjectTestCase, W : BaseProjectWhen<P, *>>(
        private val testCase: P,
        private val initProjectBlock: GradleProjectBuilder.() -> Unit,
    ) : TestCaseBuilder.Given<P, W, BaseProjectGiven<P, W>>(
        testCase,
        initProjectBlock,
    ) {

        override fun whenGradleTask(
            taskName: String,
            vararg arguments: String,
            gradleTaskBlock: GradleRunner.() -> BuildResult,
        ): W =
            createGradleRunner(testCase.projectDir, taskName, *arguments)
                .let(gradleTaskBlock)
                .let { BaseProjectWhen(testCase, it) as W }

    }

    open class BaseProjectWhen<P : BaseGradleProjectTestCase, T: BaseProjectThen<P>>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.When<P, T, BaseProjectWhen<P, T>>(
        testCase,
        buildResult,
    ) {

        override fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): T =
            BaseProjectThen(testCase, buildResult)
                .thenAssertTaskOutput(block) as T

    }

    open class BaseProjectThen<P : BaseGradleProjectTestCase>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.Then<P, BaseProjectThen<P>>(
        testCase,
        buildResult,
    )

}
