package org.eazyportal.plugin.gradle.release.testcase.builder

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.builder.TestCaseBuilder
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.VeryOldBaseScmProjectTestCase
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

object ScmProjectTestCaseBuilder {

    class ScmProjectGiven<P : VeryOldBaseScmProjectTestCase, W : ScmProjectWhen<P, *>>(
        private val testCase: P,
        private val initProjectBlock: GradleProjectBuilder.() -> Unit,
    ) : TestCaseBuilder.Given<P, W, ScmProjectGiven<P, W>>(
        testCase,
        initProjectBlock,
    ) {

        override fun whenGradleTask(
            taskName: String,
            vararg arguments: String,
            gradleTaskBlock: GradleRunner.() -> BuildResult
        ): W =
            createGradleRunner(testCase.projectFile.getFile(), taskName, *arguments)
                .let(gradleTaskBlock)
                .let { ScmProjectWhen(testCase, it) as W }

    }

    class ScmProjectWhen<P : VeryOldBaseScmProjectTestCase, T : ScmProjectThen<P>>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.When<P, T, ScmProjectWhen<P, T>>(
        testCase,
        buildResult,
    ) {

        override fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): T =
            ScmProjectThen(testCase, buildResult)
                .thenAssertTaskOutput(block) as T

    }

    class ScmProjectThen<P : VeryOldBaseScmProjectTestCase>(
        private val testCase: P,
        private val buildResult: BuildResult,
    ) : TestCaseBuilder.Then<P, ScmProjectThen<P>>(
        testCase,
        buildResult,
    ) {

        fun thenAssertProjectVersion(
            expectedVersion: Version,
        ): ScmProjectThen<P> =
            apply {
                assertThat(testCase.getProjectVersion(testCase.projectFile))
                    .isEqualTo(expectedVersion)

                // TODO: WTF?
                if (testCase is BaseMultiModuleScmProjectTestCase) {
                    assertThat(testCase.getProjectVersion(testCase.submoduleProjectFile))
                        .isEqualTo(expectedVersion)
                }
            }

        fun thenAssertScm(block: ScmAssertion.() -> Unit): ScmProjectThen<P> =
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
