package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.cli.CommandLineUtils.git
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.gradle.release.ScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.SingleModuleScmProjectBaseIntegrationTest
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PrepareRepositoryForReleaseTaskIntegrationTest {

    @Nested
    inner class SingleModuleGitProject :
        SingleModuleScmProjectBaseIntegrationTest(GitUtils),
        BasePrepareRepositoryForReleaseTaskIntegrationTest {

        override fun setupRemoteBeforeClone() {
            super.setupRemoteBeforeClone()

            // Create dev branch in origin
            remoteProjectDir.git("branch", ScmConstants.FEATURE_BRANCH)
        }

    }

    private interface BasePrepareRepositoryForReleaseTaskIntegrationTest : ScmProjectIntegrationTest {

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' should clean local changes`(testBranch: String) {
            // GIVEN
            scmUtils.checkout(projectDir, testBranch)

            scmUtils.createDummyFile(projectDir)

            // WHEN
            val actual =
                createGradleRunner(projectDir, EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                    .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :${EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")

            assertThat(scmUtils.status(projectDir))
                .contains(
                    "On branch $testBranch",
                    "Your branch is up to date with '${ScmConstants.REMOTE}/$testBranch'.",
                    "nothing to commit, working tree clean",
                )
        }

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' should clean local commits`(testBranch: String) {
            // GIVEN
            scmUtils.checkout(projectDir, testBranch)

            scmUtils.createDummyCommit(projectDir, testBranch)

            // WHEN
            val actual =
                createGradleRunner(projectDir, EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                    .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :${EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")

            assertThat(scmUtils.getCommits(projectDir))
                .doesNotContain(CHORE_COMMIT_MESSAGE)
        }

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' update release and feature branches`(testBranch: String) {
            // GIVEN
            scmUtils.createDummyCommit(
                remoteProjectDir,
                ScmConstants.RELEASE_BRANCH,
                "chore: commit on ${ScmConstants.RELEASE_BRANCH}"
            )
            scmUtils.createDummyCommit(
                remoteProjectDir,
                ScmConstants.FEATURE_BRANCH,
                "chore: commit on ${ScmConstants.FEATURE_BRANCH}"
            )

            scmUtils.checkout(projectDir, testBranch)

            // WHEN
            val actual =
                createGradleRunner(projectDir, EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                    .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :${EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")

            assertThat(
                scmUtils.getCommits(
                    remoteProjectDir,
                    ScmConstants.RELEASE_BRANCH,
                    ScmConstants.FEATURE_BRANCH,
                )
            ).contains("chore: commit on ${ScmConstants.FEATURE_BRANCH}")
                .containsExactlyElementsOf(
                    scmUtils.getCommits(
                        projectDir,
                        ScmConstants.RELEASE_BRANCH,
                        ScmConstants.FEATURE_BRANCH,
                    )
                )

            assertThat(
                scmUtils.getCommits(
                    remoteProjectDir,
                    ScmConstants.FEATURE_BRANCH,
                    ScmConstants.RELEASE_BRANCH,
                )
            ).contains("chore: commit on ${ScmConstants.RELEASE_BRANCH}")
                .containsExactlyElementsOf(
                    scmUtils.getCommits(
                        projectDir,
                        ScmConstants.FEATURE_BRANCH,
                        ScmConstants.RELEASE_BRANCH,
                    )
                )
        }

    }

}
