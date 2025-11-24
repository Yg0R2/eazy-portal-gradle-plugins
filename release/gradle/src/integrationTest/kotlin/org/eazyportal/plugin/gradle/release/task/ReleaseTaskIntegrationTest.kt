package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.BaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ReleaseTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
        originProjectDir.initializeGitAndGradleProject()
        // Workaround for using none-bare repository
        gitActions.execute(originProjectFile, "config", "receive.denyCurrentBranch", "ignore")

        gitActions.tag(originProjectFile, Version.of(INITIAL_TAG))

        gitActions.execute(
            FileSystemProjectFile(workingDir),
            "-c",
            "protocol.file.allow=always",
            "clone",
            "--recurse-submodules",
            originProjectDir.resolve(".git").path,
            PROJECT_NAME,
        )

        gitActions.execute(projectFile, "branch", FEATURE_BRANCH, "$REMOTE/$FEATURE_BRANCH")
    }

    @CsvSource(FEATURE_BRANCH, RELEASE_BRANCH)
    @ParameterizedTest
    fun `test 'release' should fail when there are no acceptable commits`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertFailedRelease(actual)
    }

    @Test
    fun `test 'release' should fail from release branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, FEATURE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, RELEASE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertFailedRelease(
            actual = actual,
            featureBranchCommits = listOf(COMMIT_MESSAGE),
        )
    }

    @Test
    fun `test 'release' should succeed from release branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, RELEASE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, RELEASE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .build()

        // THEN
        assertSucceededRelease(actual)
    }

    @Test
    fun `test 'release' should fal from feature branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, RELEASE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, FEATURE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertFailedRelease(
            actual = actual,
            releaseBranchCommits = listOf(COMMIT_MESSAGE),
        )
    }

    @Test
    fun `test 'release' should succeed from feature branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, FEATURE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, FEATURE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .build()

        // THEN
        assertSucceededRelease(actual)
    }

    @Test
    fun `test 'release with forceRelease' should succeed when there are no acceptable commits`() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease(
            actual = actual,
            releaseBranchCommits = listOf("Release version: $RELEASE_001"),
            featureBranchCommits = listOf("New SNAPSHOT version: $SNAPSHOT_002", "Release version: $RELEASE_001"),
        )
    }

    @Test
    fun `test 'release with forceRelease' should succeed from release branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, FEATURE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, RELEASE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease(
            actual = actual,
            releaseBranchCommits = listOf("Release version: $RELEASE_001"),
        )
    }

    @Test
    fun `test 'release with forceRelease' should succeed from release branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, RELEASE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, RELEASE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease(actual)
    }

    @Test
    fun `test 'release with forceRelease' should succeed from feature branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, RELEASE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, FEATURE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease(actual)
    }

    @Test
    fun `test 'release with forceRelease' should succeed from feature branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(originProjectDir, FEATURE_BRANCH, COMMIT_MESSAGE)

        gitActions.checkout(projectFile, FEATURE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease(actual)
    }

    private fun assertFailedRelease(
        actual: BuildResult,
        releaseBranchCommits: List<String> = emptyList(),
        featureBranchCommits: List<String> = emptyList(),
    ) {
        assertThat(actual.output.lines())
            .contains(
                "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                "> There are no acceptable commits.",
            )

        // assert project version
        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_001)

        assertThat(originProjectActions.getVersion())
            .isEqualTo(SNAPSHOT_001)

        assertGitRepository(releaseBranchCommits, featureBranchCommits)
    }

    private fun assertGitRepository(
        releaseBranchCommits: List<String> = emptyList(),
        featureBranchCommits: List<String> = emptyList(),
        lastTag: String = INITIAL_TAG,
    ) {
        // assert commits
        assertThat(gitActions.getCommits(projectFile, INITIAL_TAG, "$REMOTE/$RELEASE_BRANCH"))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(gitActions.getCommits(projectFile, INITIAL_TAG, "$REMOTE/$FEATURE_BRANCH"))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky
        assertThat(gitActions.getCommits(projectFile, INITIAL_TAG, RELEASE_BRANCH))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(gitActions.getCommits(projectFile, INITIAL_TAG, FEATURE_BRANCH))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky

        assertThat(gitActions.getCommits(originProjectFile, INITIAL_TAG, RELEASE_BRANCH))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(gitActions.getCommits(originProjectFile, INITIAL_TAG, FEATURE_BRANCH))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky

        // assert tags
        assertThat(gitActions.getLastTag(projectFile, RELEASE_BRANCH))
            .isEqualTo(lastTag)

        assertThat(gitActions.getLastTag(originProjectFile, RELEASE_BRANCH))
            .isEqualTo(lastTag)

        val expectedTags = setOf(INITIAL_TAG, lastTag)

        gitActions.execute(projectFile, "tag")
            .split(System.lineSeparator())
            .let { assertThat(it) }
            .containsExactlyElementsOf(expectedTags)

        gitActions.execute(originProjectFile, "tag")
            .split(System.lineSeparator())
            .let { assertThat(it) }
            .containsExactlyElementsOf(expectedTags)
    }

    private fun assertSucceededRelease(
        actual: BuildResult,
        releaseBranchCommits: List<String> = listOf(
            "Release version: $RELEASE_001",
            COMMIT_MESSAGE,
        ),
        featureBranchCommits: List<String> = listOf(
            "New SNAPSHOT version: $SNAPSHOT_002",
            "Release version: $RELEASE_001",
            COMMIT_MESSAGE,
        ),
    ) {
        assertThat(actual.output.lines())
            .contains(
                "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                "> Task :build",
                "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$UPDATE_SCM_TASK_NAME",
                "> Task :$RELEASE_TASK_NAME",
            )

        // assert project version
        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_002)

        assertThat(originProjectActions.getVersion())
            .isEqualTo(SNAPSHOT_002)

        assertGitRepository(releaseBranchCommits, featureBranchCommits, RELEASE_001.toString())
    }

    companion object {
        private const val COMMIT_MESSAGE = "fix: dummy commit"
        private val INITIAL_TAG = Version(0, 0, 0).toString()
    }

}
