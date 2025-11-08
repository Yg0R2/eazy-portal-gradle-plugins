package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import org.eazyportal.plugin.gradle.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.gradle.release.core.scm.ScmConstants.MAIN_BRANCH
import org.eazyportal.plugin.gradle.release.core.scm.ScmConstants.REMOTE
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.util.UUID

@TestMethodOrder(OrderAnnotation::class)
class PrepareRepositoryForReleaseTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
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

    @AfterEach
    fun tearDown() {
        gitActions.execute(projectFile, "reset", "--hard")
    }

    @CsvSource(MAIN_BRANCH, FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' should checkout to feature branch`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertTaskExecutionAndCleanFeatureBranch(actual.output.lines())
    }

    @CsvSource(MAIN_BRANCH, FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' should clean project and checkout to feature branch`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        projectFile.createDummyFile()

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertTaskExecutionAndCleanFeatureBranch(actual.output.lines())
    }

    @CsvSource(MAIN_BRANCH, FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' should ignore local commits and checkout to feature branch`(
        testBranch: String,
        testInfo: TestInfo
    ) {
        // GIVEN
        val commitMessage = projectFile.createDummyComment(testBranch, testInfo)

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertTaskExecutionAndCleanFeatureBranch(actual.output.lines())

        gitActions.getCommits(projectFile)
            .let(Assertions::assertThat)
            .hasSize(2)
            .doesNotContain(commitMessage)
        gitActions.getCommits(projectFile, FEATURE_BRANCH, MAIN_BRANCH)
            .let(Assertions::assertThat)
            .isEmpty()
        gitActions.getCommits(projectFile, MAIN_BRANCH, FEATURE_BRANCH)
            .let(Assertions::assertThat)
            .isEmpty()
    }

    @Test
    fun `test 'run' should fetch changes from origin main branch`(testInfo: TestInfo) {
        // GIVEN
        val commitMessage = originProjectFile.createDummyComment(MAIN_BRANCH, testInfo)

        gitActions.checkout(projectFile, MAIN_BRANCH)

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertTaskExecutionAndCleanFeatureBranch(actual.output.lines())

        gitActions.getCommits(projectFile)
            .let(Assertions::assertThat)
            .doesNotContain(commitMessage)
        gitActions.getCommits(projectFile, FEATURE_BRANCH, MAIN_BRANCH)
            .let(Assertions::assertThat)
            .hasSize(1)
            .contains(commitMessage)
        gitActions.getCommits(projectFile, MAIN_BRANCH, FEATURE_BRANCH)
            .let(Assertions::assertThat)
            .isEmpty()
    }

    @Test
    fun `test 'run' should fetch changes from origin feature branch`(testInfo: TestInfo) {
        // GIVEN
        val commitMessage = originProjectFile.createDummyComment(FEATURE_BRANCH, testInfo)

        // WHEN
        val actual = createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .build()

        // THEN
        assertTaskExecutionAndCleanFeatureBranch(actual.output.lines())

        gitActions.getCommits(projectFile)
            .let(Assertions::assertThat)
            .contains(commitMessage)
    }

    private fun assertTaskExecutionAndCleanFeatureBranch(taskExecutionOutput: List<String>) {
        assertThat(taskExecutionOutput)
            .contains(
                "> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME",
            )

        gitActions.execute(projectFile, "status")
            .split("\r?\n".toRegex())
            .let(Assertions::assertThat)
            .contains(
                "On branch $FEATURE_BRANCH",
                "Your branch is up to date with '$REMOTE/$FEATURE_BRANCH'.",
                "nothing to commit, working tree clean",
            )
    }

    private fun ProjectFile<File>.createDummyFile() {
        resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    private fun ProjectFile<File>.createDummyComment(
        branch: String,
        testInfo: TestInfo,
    ): String {
        gitActions.checkout(this, branch)

        createDummyFile()

        gitActions.add(this, DUMMY_FILE_NAME)

        return "chore: update $DUMMY_FILE_NAME for `${testInfo.testMethod.get().name}`"
            .also { gitActions.commit(this, it) }
    }

    companion object {
        private const val DUMMY_FILE_NAME = "dummy.txt"
    }

}
