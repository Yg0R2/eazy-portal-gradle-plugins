package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleCustomFlowScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleGitFlowScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleTrunkFlowScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.asd.BaseSingleModuleCustomFlowScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.asd.BaseSingleModuleGitFlowScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.asd.BaseSingleModuleTrunkFlowScmProjectIntegrationTest
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.testkit.runner.BuildResult
import java.io.File

object TestBuilder {

    class Given<T : BaseScmProjectIntegrationTest>(
        private val baseTest: T,
    ) {

        fun whenGradleTaskSucceeds(taskName: String): When<T> {
            baseTest.initializeProject()

            return createGradleRunner(baseTest.projectFile.getFile(), taskName)
                .build()
                .let { When(baseTest, it) }
        }

    }

    class When<T : BaseScmProjectIntegrationTest>(
        private val baseTest: T,
        private val result: BuildResult,
    ) {

        fun thenAssert(assertBlock: Then<T>.() -> Unit) {
            assertBlock(Then(baseTest, result))
        }

    }

    class Then<T : BaseScmProjectIntegrationTest>(
        private val baseTest: T,
        private val result: BuildResult,
    ) {

        fun taskOutput(block: ListAssert<String>.() -> Unit) {
            block(assertThat(result.output.lines()))
        }

        fun scmStatus(block: ListAssert<String>.() -> Unit) {
            block(assertThat(baseTest.scmActions.status(baseTest.projectFile)))
        }

    }

    fun givenSingleModuleGitFlowGitProject(
        workingDir: File,
        configureProjectBlock: BaseSingleModuleGitFlowScmProjectIntegrationTest.() -> Unit = {},
    ): Given<BaseSingleModuleGitFlowScmProjectIntegrationTest> =
        object : BaseSingleModuleGitFlowScmProjectIntegrationTest(
            ScmConfig.GIT_FLOW,
            TestGitActions(CommandLineExecutor()),
        ) {

            init {
                this.workingDir = workingDir
            }

            override fun configureProject() {
                configureProjectBlock()
            }

        }.let { Given(it) }

    fun givenSingleModuleTrunkFlowGitProject(): Given<BaseSingleModuleTrunkFlowScmProjectIntegrationTest> =
        TODO("Not implemented yet")

    fun givenSingleModuleCustomFlowGitProject(): Given<BaseSingleModuleCustomFlowScmProjectIntegrationTest> =
        TODO("Not implemented yet")

    fun givenMultiModuleGitFlowGitProject(): Given<BaseMultiModuleGitFlowScmProjectIntegrationTest> =
        TODO("Not implemented yet")

    fun givenMultiModuleTrunkFlowGitProject(): Given<BaseMultiModuleTrunkFlowScmProjectIntegrationTest> =
        TODO("Not implemented yet")

    fun givenMultiModuleCustomFlowGitProject(): Given<BaseMultiModuleCustomFlowScmProjectIntegrationTest> =
        TODO("Not implemented yet")

}