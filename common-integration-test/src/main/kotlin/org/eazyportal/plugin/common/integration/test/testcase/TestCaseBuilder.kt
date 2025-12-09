package org.eazyportal.plugin.common.integration.test.testcase

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

object TestCaseBuilder {

    class Given<T: BaseProjectTestCase>(
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
            gradleTaskBlock: T.(GradleRunner) -> BuildResult,
        ): When<T> =
            When(
                testCase,
                testCase.gradleTaskBlock(createGradleRunner(testCase.projectDir, taskName, *arguments))
            )

        fun whenGradleTaskFails(
            taskName: String,
            vararg arguments: String,
        ): When<T> =
            createGradleRunner(testCase.projectDir, taskName, *arguments)
                .buildAndFail()
                .let { When(testCase, it) }

        fun whenGradleTaskSucceeds(
            taskName: String,
            vararg arguments: String,
        ): When<T> =
            createGradleRunner(testCase.projectDir, taskName, *arguments)
                .build()
                .let { When(testCase, it) }

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
            apply { block(assertThat(buildResult.output.lines())) }

    }

    inline fun <reified T : BaseProjectTestCase> givenTestCase(
        workingDir: File,
        noinline initProjectBlock: GradleProjectBuilder.() -> Unit = {},
    ): Given<out T> =
        Given(
            T::class.java.getDeclaredConstructor(File::class.java).newInstance(workingDir),
            initProjectBlock,
        )

}
