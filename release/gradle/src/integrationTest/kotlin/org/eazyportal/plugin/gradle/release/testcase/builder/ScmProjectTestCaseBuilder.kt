package org.eazyportal.plugin.gradle.release.testcase.builder

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.builder.TestCaseBuilder
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.ScmAssertion
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.Then
import org.eazyportal.plugin.gradle.release.testcase.BaseScmProjectTestCase
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

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
    ) {

        fun thenScmAssert(block: (ScmAssertion) -> Unit): ScmProjectThen<P> =
            apply {
                block(ScmAssertion(testCase.scmActions, testCase.scmConfig))
            }

        class ScmAssertion(
            private val scmActions: TestScmActions<File>,
            private val scmConfig: ScmConfig,
        ) {

            fun commitsIn(
                projectFile: ProjectFile<File>,
                block: ListAssert<String>.() -> Unit,
            ) {
                block(assertThat(scmActions.getCommits(projectFile)))
            }

            fun compareCommitsIn(
                projectFile: ProjectFile<File>,
                remoteProjectFile: ProjectFile<File>,
                block: ListAssert<String>.() -> Unit = {},
            ) {
                assertThat(scmActions.getCommits(projectFile))
                    .containsExactlyElementsOf(scmActions.getCommits(remoteProjectFile))
                    .block()
            }

            fun statusCleanIn(
                projectFile: ProjectFile<File>,
                branch: String,
            ) {
                statusIn(projectFile) {
                    containsExactly(
                        "On branch $branch",
                        "Your branch is up to date with '${scmConfig.remote}/$branch'.",
                        "nothing to commit, working tree clean",
                    )
                }
            }

            fun statusIn(
                projectFile: ProjectFile<File>,
                block: ListAssert<String>.() -> Unit,
            ) {
                block(assertThat(scmActions.status(projectFile)))
            }

        }
    }

}
