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

        fun thenAssert(assertBlock: Then<T>.() -> Unit) {
            assertBlock(Then(baseTest, result))
        }

    }

    class Then<T : BaseScmProjectTestCase>(
        private val baseTest: T,
        private val buildResult: BuildResult,
    ) {

        fun taskOutput(block: ThenContext<ListAssert<String>>.() -> Unit) {
            block(
                ThenContext(
                    scmActions = baseTest.scmActions,
                    scmConfig = baseTest.scmConfig,
                    result = assertThat(buildResult.output.lines()),
                )
            )
        }

        fun scmStatus(block: ThenContext<ListAssert<String>>.() -> Unit) {
            block(
                ThenContext(
                    scmActions = baseTest.scmActions,
                    scmConfig = baseTest.scmConfig,
                    result = assertThat(baseTest.scmActions.status(baseTest.projectFile))
                )
            )
        }

        class ThenContext<A : Any>(
            val scmActions: TestScmActions<File>,
            val scmConfig: ScmConfig,
            val result: A,
        )

    }

    fun <T : BaseScmProjectTestCase> givenTestCase(
        clazz: KClass<out T>,
        workingDir: File,
        configureProjectBlock: T.() -> Unit = {},
    ): Given<T> =
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