package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.cli.CommandLineUtils.git
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.gradle.release.MultiModuleScmProjectBaseIntegrationTest
import org.eazyportal.plugin.gradle.release.MultiModuleScmProjectBaseIntegrationTest1
import org.eazyportal.plugin.gradle.release.ScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.SingleModuleScmProjectBaseIntegrationTest
import org.eazyportal.plugin.gradle.release.SingleModuleScmProjectBaseIntegrationTest1
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class PrepareRepositoryForReleaseTaskIntegrationTest {

    @Nested
    inner class MultiModuleGitFlow : MultiModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.GIT_FLOW,
    ) {

        override fun setUpBeforeClone() {
            // Create remote feature branch
            scmActions.execute(remoteProjectFile, "branch", ScmConstants.FEATURE_BRANCH)
            scmActions.execute(remoteSubmoduleProjectFile, "branch", ScmConstants.FEATURE_BRANCH)
        }

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' should clean local changes`(testBranch: String) {
            // GIVEN
            scmActions.checkout(projectFile, testBranch)

            createDummyFile(submoduleProjectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch $testBranch",
                    "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
                    "nothing to commit, working tree clean",
                )
        }

    }

    @Nested
    inner class MultiModuleTrunkFlow : MultiModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.TRUNK_BASED_FLOW,
    ) {

        override fun initializeRepository(initProjectFile: ProjectFile<File>) {
            GradleProjectBuilder(
                projectDir = initProjectFile.getFile(),
                projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
            ).withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
            ).build()

            scmActions.initializeRepository(initProjectFile)
        }

        @Test
        fun `test 'run' should clean local changes`() {
            // GIVEN
            createDummyFile(submoduleProjectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch ${scmConfig.releaseBranch}",
                    "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                    "nothing to commit, working tree clean",
                )
        }

    }

    @Nested
    inner class SingleModuleGitFlow : SingleModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.GIT_FLOW,
    ) {

        override fun setUpBeforeClone() {
            // Create remote feature branch
            scmActions.execute(remoteProjectFile, "branch", ScmConstants.FEATURE_BRANCH)
        }

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' should clean local changes`(testBranch: String) {
            // GIVEN
            scmActions.checkout(projectFile, testBranch)

            createDummyFile(projectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch $testBranch",
                    "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
                    "nothing to commit, working tree clean",
                )
        }
    }

    @Nested
    inner class SingleModuleTrunkFlow : SingleModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.TRUNK_BASED_FLOW,
    ) {

        override fun initializeRepository(initProjectFile: ProjectFile<File>) {
            GradleProjectBuilder(
                projectDir = initProjectFile.getFile(),
                projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
            ).withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
            ).build()

            scmActions.initializeRepository(initProjectFile)
        }

        @Test
        fun `test 'run' should clean local changes`() {
            // GIVEN
            createDummyFile(projectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch ${scmConfig.releaseBranch}",
                    "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                    "nothing to commit, working tree clean",
                )
        }

//        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
//        @ParameterizedTest
//        fun `test 'run' should clean local commits`(testBranch: String) {
//            // GIVEN
//            scmUtils.checkout(projectDir, testBranch)
//
//            scmUtils.createDummyCommit(projectDir, testBranch)
//
//            // WHEN
//            val actual =
//                createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//                    .build()
//
//            // THEN
//            assertThat(actual.output.lines())
//                .contains("> Task :${PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")
//
//            assertThat(scmUtils.getCommits(projectDir))
//                .doesNotContain(CHORE_COMMIT_MESSAGE)
//        }
//
//        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
//        @ParameterizedTest
//        fun `test 'run' update release and feature branches`(testBranch: String) {
//            // GIVEN
//            scmUtils.createDummyCommit(
//                remoteProjectFile,
//                ScmConstants.RELEASE_BRANCH,
//                "chore: commit on ${ScmConstants.RELEASE_BRANCH}"
//            )
//            scmUtils.createDummyCommit(
//                remoteProjectFile,
//                ScmConstants.FEATURE_BRANCH,
//                "chore: commit on ${ScmConstants.FEATURE_BRANCH}"
//            )
//
//            scmUtils.checkout(projectDir, testBranch)
//
//            // WHEN
//            val actual =
//                createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//                    .build()
//
//            // THEN
//            assertThat(actual.output.lines())
//                .contains("> Task :${PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")
//
//            assertThat(
//                scmUtils.getCommits(
//                    remoteProjectFile,
//                    ScmConstants.RELEASE_BRANCH,
//                    ScmConstants.FEATURE_BRANCH,
//                )
//            ).contains("chore: commit on ${ScmConstants.FEATURE_BRANCH}")
//                .containsExactlyElementsOf(
//                    scmUtils.getCommits(
//                        projectDir,
//                        ScmConstants.RELEASE_BRANCH,
//                        ScmConstants.FEATURE_BRANCH,
//                    )
//                )
//
//            assertThat(
//                scmUtils.getCommits(
//                    remoteProjectFile,
//                    ScmConstants.FEATURE_BRANCH,
//                    ScmConstants.RELEASE_BRANCH,
//                )
//            ).contains("chore: commit on ${ScmConstants.RELEASE_BRANCH}")
//                .containsExactlyElementsOf(
//                    scmUtils.getCommits(
//                        projectDir,
//                        ScmConstants.FEATURE_BRANCH,
//                        ScmConstants.RELEASE_BRANCH,
//                    )
//                )
//        }

    }

}
