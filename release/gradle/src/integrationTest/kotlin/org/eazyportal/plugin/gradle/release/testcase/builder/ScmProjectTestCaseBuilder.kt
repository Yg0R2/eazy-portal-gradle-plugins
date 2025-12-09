package org.eazyportal.plugin.gradle.release.testcase.builder

import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.builder.TestCaseBuilder
import org.eazyportal.plugin.gradle.release.testcase.BaseScmProjectTestCase
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

object ScmProjectTestCaseBuilder {

    class ScmProjectGiven<P : BaseScmProjectTestCase>(
        private val testCase: P,
        private val initProjectBlock: GradleProjectBuilder.() -> Unit,
    ) : TestCaseBuilder.Given<P, ScmProjectGiven<P>>(
        testCase,
        initProjectBlock,
    ) {

        override fun whenGradleTask(
            taskName: String,
            vararg arguments: String,
            gradleTaskBlock: GradleRunner.() -> BuildResult
        ): ScmProjectWhen<P> =
            createGradleRunner(testCase.projectFile.getFile(), taskName, *arguments)
                .let(gradleTaskBlock)
                .let { ScmProjectWhen(testCase, it) }

    }

    class ScmProjectWhen<P : BaseScmProjectTestCase>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.When<P, ScmProjectWhen<P>>(
        testCase,
        buildResult,
    ) {

        override fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): ScmProjectThen<P> =
            ScmProjectThen(testCase, buildResult)
                .thenAssertTaskOutput(block)

    }

    class ScmProjectThen<P : BaseScmProjectTestCase>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.Then<P, ScmProjectThen<P>>(
        testCase,
        buildResult,
    )

}
