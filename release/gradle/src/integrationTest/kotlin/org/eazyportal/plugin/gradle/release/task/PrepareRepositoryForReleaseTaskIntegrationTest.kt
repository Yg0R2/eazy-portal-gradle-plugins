package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleTestFixtures
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.BaseIntegrationTest
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PrepareRepositoryForReleaseTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
        originProjectDir.initializeGitAndGradleProject()

        gitActions.execute(
            FileSystemProjectFile(workingDir),
            "-c",
            "protocol.file.allow=always",
            "clone",
            "--recurse-submodules",
            originProjectDir.resolve(".git").path,
            GradleTestFixtures.PROJECT_NAME,
        )
    }

    @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' should clean local changes`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        scmUtils.createDummyFile(projectDir)

        // WHEN
        val actual = createGradleRunner(projectDir, EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains("> Task :${EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")

        assertThat(gitActions.execute(projectFile, "status"))
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
        gitActions.checkout(projectFile, testBranch)

        scmUtils.createDummyCommit(projectDir, testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains("> Task :${EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")

        assertThat(gitActions.getCommits(projectFile))
            .doesNotContain(CHORE_COMMIT_MESSAGE)
    }

    @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' update release and feature branches`(testBranch: String) {
        // GIVEN
        scmUtils.createDummyCommit(
            originProjectDir,
            ScmConstants.RELEASE_BRANCH,
            "chore: commit on ${ScmConstants.RELEASE_BRANCH}"
        )
        scmUtils.createDummyCommit(
            originProjectDir,
            ScmConstants.FEATURE_BRANCH,
            "chore: commit on ${ScmConstants.FEATURE_BRANCH}"
        )

        gitActions.checkout(projectFile, testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains("> Task :${EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")

        assertThat(
            gitActions.getCommits(
                originProjectFile,
                ScmConstants.RELEASE_BRANCH,
                ScmConstants.FEATURE_BRANCH
            )
        ).contains("chore: commit on ${ScmConstants.FEATURE_BRANCH}")
            .containsExactlyElementsOf(
                gitActions.getCommits(
                    projectFile,
                    ScmConstants.RELEASE_BRANCH,
                    ScmConstants.FEATURE_BRANCH
                )
            )

        assertThat(
            gitActions.getCommits(
                originProjectFile,
                ScmConstants.FEATURE_BRANCH,
                ScmConstants.RELEASE_BRANCH
            )
        ).contains("chore: commit on ${ScmConstants.RELEASE_BRANCH}")
            .containsExactlyElementsOf(
                gitActions.getCommits(
                    projectFile,
                    ScmConstants.FEATURE_BRANCH,
                    ScmConstants.RELEASE_BRANCH
                )
            )
    }

}