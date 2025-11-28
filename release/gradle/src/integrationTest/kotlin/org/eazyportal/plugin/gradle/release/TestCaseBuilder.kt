package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleCustomFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseSingleModuleCustomFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseSingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.testkit.runner.BuildResult
import java.io.File

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
        private val result: BuildResult,
    ) {

        fun taskOutput(block: ListAssert<String>.() -> Unit) {
            block(assertThat(this@Then.result.output.lines()))
        }

        fun scmStatus(block: ThenContext<ListAssert<String>>.() -> Unit) {
            block(
                ThenContext(
                    baseTest.scmActions,
                    baseTest.scmConfig,
                    assertThat(baseTest.scmActions.status(baseTest.projectFile))
                )
            )
        }

        class ThenContext<A : Any>(
            val scmActions: TestScmActions<File>,
            val scmConfig: ScmConfig,
            val result: A,
        )

    }

    inline fun <reified T : BaseScmProjectTestCase> givenTestCase(
        workingDir: File,
        noinline configureProjectBlock: T.() -> Unit = {},
    ): Given<T> =
        Given(
            T::class.java.getDeclaredConstructor(File::class.java).newInstance(workingDir),
            configureProjectBlock,
        )

    fun givenSingleModuleGitFlowGitProject(
        workingDir: File,
        configureProjectBlock: SingleModuleGitFlowScmProjectTestCase.() -> Unit = {},
    ): Given<SingleModuleGitFlowScmProjectTestCase> =
        Given(SingleModuleGitFlowScmProjectTestCase(workingDir), configureProjectBlock)

    fun givenSingleModuleTrunkFlowGitProject(): Given<BaseSingleModuleTrunkFlowScmProjectTestCase> =
        TODO("Not implemented yet")

    fun givenSingleModuleCustomFlowGitProject(): Given<BaseSingleModuleCustomFlowScmProjectTestCase> =
        TODO("Not implemented yet")

    fun givenMultiModuleGitFlowGitProject(): Given<BaseMultiModuleGitFlowScmProjectTestCase> =
        TODO("Not implemented yet")

    fun givenMultiModuleTrunkFlowGitProject(): Given<BaseMultiModuleTrunkFlowScmProjectTestCase> =
        TODO("Not implemented yet")

    fun givenMultiModuleCustomFlowGitProject(): Given<BaseMultiModuleCustomFlowScmProjectTestCase> =
        TODO("Not implemented yet")

}