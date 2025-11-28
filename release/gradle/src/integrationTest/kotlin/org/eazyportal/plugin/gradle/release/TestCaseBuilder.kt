package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.gradle.testkit.runner.BuildResult
import java.io.File
import kotlin.reflect.KClass

object TestCaseBuilder {

    class Given<T : BaseScmProjectTestCase>(
        private val testCase: T,
        configureProjectBlock: T.() -> Unit,
    ) {

        init {
            testCase.initializeProject()

            configureProjectBlock(testCase)
        }

        fun whenGradleTaskSucceeds(taskName: String): When<T> =
            createGradleRunner(testCase.projectFile.getFile(), taskName)
                .build()
                .let { When(testCase, it) }

    }

    class When<T : BaseScmProjectTestCase>(
        private val testCase: T,
        private val result: BuildResult,
    ) {

        fun thenAssert(assertBlock: T.(Then<T>) -> Unit) {
            testCase.assertBlock(Then(testCase, result))
        }

    }

    class Then<T: BaseScmProjectTestCase>(
        private val testCase: T,
        private val buildResult: BuildResult,
    ) {

        fun scmCommits(block: ListAssert<String>.() -> Unit) {
            block(assertThat(testCase.scmActions.getCommits(testCase.projectFile)))
        }

        fun scmStatus(block: ListAssert<String>.() -> Unit) {
            block(
                assertThat(testCase.scmActions.status(testCase.projectFile))
            )
        }

        fun taskOutput(block: ListAssert<String>.() -> Unit) {
            block(
                assertThat(buildResult.output.lines()),
            )
        }

    }

    fun <T : BaseScmProjectTestCase> givenTestCase(
        clazz: KClass<out T>,
        workingDir: File,
        configureProjectBlock: T.() -> Unit = {},
    ): Given<out T> =
        Given(
            clazz.java.getDeclaredConstructor(File::class.java).newInstance(workingDir),
            configureProjectBlock,
        )

}