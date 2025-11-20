package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
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
            PROJECT_NAME
        )
    }

    @CsvSource(RELEASE_BRANCH, FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' should clean local changes`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        projectFile.createDummyFile()

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

        assertThat(gitActions.execute(projectFile, "status"))
            .contains(
                "On branch $testBranch",
                "Your branch is up to date with '$REMOTE/$testBranch'.",
                "nothing to commit, working tree clean",
            )
    }

    @CsvSource(RELEASE_BRANCH, FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' should clean local commits`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        projectFile.createDummyComment(testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

        assertThat(gitActions.getCommits(projectFile))
            .doesNotContain(DUMMY_COMMIT_MESSAGE)
    }

    @CsvSource(RELEASE_BRANCH, FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' update release and feature branches`(testBranch: String) {
        // GIVEN
        originProjectFile.createDummyComment(RELEASE_BRANCH, "chore: commit on $RELEASE_BRANCH")
        originProjectFile.createDummyComment(FEATURE_BRANCH, "chore: commit on $FEATURE_BRANCH")

        gitActions.checkout(projectFile, testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

        assertThat(gitActions.getCommits(originProjectFile, RELEASE_BRANCH, FEATURE_BRANCH))
            .contains("chore: commit on $FEATURE_BRANCH")
            .containsExactlyElementsOf(gitActions.getCommits(projectFile, RELEASE_BRANCH, FEATURE_BRANCH))

        assertThat(gitActions.getCommits(originProjectFile, FEATURE_BRANCH, RELEASE_BRANCH))
            .contains("chore: commit on $RELEASE_BRANCH")
            .containsExactlyElementsOf(gitActions.getCommits(projectFile, FEATURE_BRANCH, RELEASE_BRANCH))
    }

}
