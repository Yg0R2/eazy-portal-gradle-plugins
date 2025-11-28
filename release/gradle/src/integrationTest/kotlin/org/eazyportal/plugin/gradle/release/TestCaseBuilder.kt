package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.testkit.runner.BuildResult
import java.io.File
import kotlin.reflect.KClass

object TestCaseBuilder {

    class Given<T : BaseScmProjectTestCase>(
        private val baseTest: T,
        configureProjectBlock: T.() -> Unit,
    ) {

        init {
            baseTest.initializeProject()

            configureProjectBlock(baseTest)
        }

        fun whenGradleTaskSucceeds(taskName: String): When<T> =
            createGradleRunner(baseTest.projectFile.getFile(), taskName)
                .build()
                .let { When(baseTest, it) }

    }

    class When<T : BaseScmProjectTestCase>(
        private val baseTest: T,
        private val result: BuildResult,
    ) {

        fun thenAssert(assertBlock: T.() -> Unit): Then<T> =
            Then(baseTest, result)

    }

    class Then<T : BaseScmProjectTestCase>(
        private val baseTest: T,
        private val buildResult: BuildResult,
    ) {

//        val scmActions: TestScmActions<File> = baseTest.scmActions
//        val scmConfig: ScmConfig = baseTest.scmConfig

        fun scmCommits(block: ListAssert<String>.() -> Unit) {
            block(assertThat(baseTest.scmActions.getCommits(baseTest.projectFile)))
        }

        fun scmStatus(block: ListAssert<String>.() -> Unit) {
            block(
                assertThat(baseTest.scmActions.status(baseTest.projectFile))
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

    inline fun <reified T : BaseScmProjectTestCase> givenTestCase(
        workingDir: File,
        noinline configureProjectBlock: T.() -> Unit = {},
    ): Given<T> =
        Given(
            T::class.java.getDeclaredConstructor(File::class.java).newInstance(workingDir),
            configureProjectBlock,
        )

}