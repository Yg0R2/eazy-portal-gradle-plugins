package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.FIX_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_TAG
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class SingleProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ProjectBaseIntegrationTest(scmUtils) {

    @BeforeEach
    override fun setUpRepositories(@TempDir tempDir: File) {
        super.setUpRepositories(tempDir)

        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmUtils.initializeRepository(remoteProjectDir)
        scmUtils.clone(remoteProjectDir, projectDir)
    }

}

abstract class MultiProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ProjectBaseIntegrationTest(scmUtils) {

}

abstract class ProjectBaseIntegrationTest(
    protected open val scmUtils: ScmUtils,
) {

    protected val projectDir: File
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

    protected val remoteProjectDir: File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/$PROJECT_NAME")
            .also { it.mkdirs() }

    protected lateinit var workingDir: File

    private val projectActionsMap = mutableMapOf<String, ProjectActions<File>>()

    @BeforeEach
    open fun setUpRepositories(@TempDir tempDir: File) {
        workingDir = tempDir
    }

    protected fun assertFailedRelease(
        releaseBranchCommits: List<String> = emptyList(),
        featureBranchCommits: List<String> = emptyList(),
    ) {
        scmUtils.clean(projectDir)
        scmUtils.clean(remoteProjectDir)

        // assert project version
        listOf(RELEASE_BRANCH, FEATURE_BRANCH).forEach {
            assertThat(getProjectVersion(projectDir, it))
                .isEqualTo(SNAPSHOT_001)
            assertThat(getProjectVersion(remoteProjectDir, it))
                .isEqualTo(SNAPSHOT_001)
        }

        assertRepositories(releaseBranchCommits, featureBranchCommits)
    }

    protected fun assertSucceededRelease(
        releaseBranchCommits: List<String> = listOf(
            "Release version: $RELEASE_001",
            FIX_COMMIT_MESSAGE,
        ),
        featureBranchCommits: List<String> = listOf(
            "New SNAPSHOT version: $SNAPSHOT_002",
            "Release version: $RELEASE_001",
            FIX_COMMIT_MESSAGE,
        ),
    ) {
        scmUtils.clean(projectDir)
        scmUtils.clean(remoteProjectDir)

        // assert project version
        assertThat(getProjectVersion(projectDir, FEATURE_BRANCH))
            .isEqualTo(SNAPSHOT_002)
        assertThat(getProjectVersion(remoteProjectDir, FEATURE_BRANCH))
            .isEqualTo(SNAPSHOT_002)

        assertThat(getProjectVersion(projectDir, RELEASE_BRANCH))
            .isEqualTo(RELEASE_001)
        assertThat(getProjectVersion(remoteProjectDir, RELEASE_BRANCH))
            .isEqualTo(RELEASE_001)

        assertRepositories(releaseBranchCommits, featureBranchCommits, RELEASE_001.toString())
    }

    private fun getProjectVersion(projectDir: File, branch: String?): Version {
        if (branch != null) {
            scmUtils.checkout(projectDir, branch)
        }

        return projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.getVersion()
    }

    private fun assertRepositories(
        releaseBranchCommits: List<String> = emptyList(),
        featureBranchCommits: List<String> = emptyList(),
        lastTag: String = INITIAL_TAG,
    ) {
        // assert commits
        assertThat(scmUtils.getCommits(projectDir, INITIAL_TAG, "$REMOTE/$RELEASE_BRANCH"))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(scmUtils.getCommits(projectDir, INITIAL_TAG, "$REMOTE/$FEATURE_BRANCH"))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky
        assertThat(scmUtils.getCommits(projectDir, INITIAL_TAG, RELEASE_BRANCH))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(scmUtils.getCommits(projectDir, INITIAL_TAG, FEATURE_BRANCH))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky

        assertThat(scmUtils.getCommits(remoteProjectDir, INITIAL_TAG, RELEASE_BRANCH))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(scmUtils.getCommits(remoteProjectDir, INITIAL_TAG, FEATURE_BRANCH))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky

        // assert tags
        assertThat(scmUtils.getLastTag(projectDir, RELEASE_BRANCH))
            .isEqualTo(lastTag)

        assertThat(scmUtils.getLastTag(remoteProjectDir, RELEASE_BRANCH))
            .isEqualTo(lastTag)

        val expectedTags = setOf(lastTag, INITIAL_TAG)

        assertThat(scmUtils.getTags(projectDir))
            .containsExactlyInAnyOrderElementsOf(expectedTags) // flaky

        assertThat(scmUtils.getTags(remoteProjectDir))
            .containsExactlyInAnyOrderElementsOf(expectedTags) // flaky
    }

}
