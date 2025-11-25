package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ScmTestFixtures.FIX_COMMIT_MESSAGE
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.gradle.release.SingleProjectBaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ReleaseTaskIntegrationTest : SingleProjectBaseIntegrationTest(GitUtils) {

    @CsvSource(FEATURE_BRANCH, RELEASE_BRANCH)
    @ParameterizedTest
    fun `test 'release' should fail when there are no acceptable commits`(testBranch: String) {
        // GIVEN
        scmUtils.checkout(projectDir, testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertFailedRelease()
    }

    @Test
    fun `test 'release' should fail from release branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, FEATURE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, RELEASE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertFailedRelease(
            featureBranchCommits = listOf(FIX_COMMIT_MESSAGE),
        )
    }

    @Test
    fun `test 'release' should succeed from release branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, RELEASE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, RELEASE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .build()

        // THEN
        assertSucceededRelease()
    }

    @Test
    fun `test 'release' should fal from feature branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, RELEASE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, FEATURE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertFailedRelease(
            releaseBranchCommits = listOf(FIX_COMMIT_MESSAGE),
        )
    }

    @Test
    fun `test 'release' should succeed from feature branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, FEATURE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, FEATURE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .build()

        // THEN
        assertSucceededRelease()
    }

    @Test
    fun `test 'release with forceRelease' should succeed when there are no acceptable commits`() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease(
            releaseBranchCommits = listOf("Release version: $RELEASE_001"),
            featureBranchCommits = listOf("New SNAPSHOT version: $SNAPSHOT_002", "Release version: $RELEASE_001"),
        )
    }

    @Test
    fun `test 'release with forceRelease' should succeed from release branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, FEATURE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, RELEASE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease(
            releaseBranchCommits = listOf("Release version: $RELEASE_001"),
        )
    }

    @Test
    fun `test 'release with forceRelease' should succeed from release branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, RELEASE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, RELEASE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease()
    }

    @Test
    fun `test 'release with forceRelease' should succeed from feature branch when there are acceptable commits on release branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, RELEASE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, FEATURE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease()
    }

    @Test
    fun `test 'release with forceRelease' should succeed from feature branch when there are acceptable commits on feature branch`() {
        // GIVEN
        scmUtils.createDummyCommit(remoteProjectDir, FEATURE_BRANCH, FIX_COMMIT_MESSAGE)

        scmUtils.checkout(projectDir, FEATURE_BRANCH)
        scmUtils.fetch(projectDir, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertSucceededRelease()
    }

}
