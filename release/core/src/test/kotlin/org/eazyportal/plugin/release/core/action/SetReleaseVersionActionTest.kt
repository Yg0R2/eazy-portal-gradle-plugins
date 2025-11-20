package org.eazyportal.plugin.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verifySequence
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.exception.ScmActionException
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.release.core.version.model.VersionIncrement
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class SetReleaseVersionActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActions: ProjectActions<File>

    @MockK
    private lateinit var releaseVersionProvider: ReleaseVersionProvider

    @MockK
    private lateinit var scmActions: ScmActions<File>

    @MockK
    private lateinit var versionIncrementProvider: VersionIncrementProvider

    private lateinit var underTest: SetReleaseVersionAction<File>

    @Test
    fun test_execute_withGitFlow() {
        // GIVEN
        every { scmActions.getLastTag(projectFile) } returns GIT_TAG
        every { scmActions.getCommits(projectFile, GIT_TAG) } returns COMMITS

        subProjectFiles.forEach {
            every { scmActions.getLastTag(it) } throws ScmActionException(null)
            every { scmActions.getCommits(it, null) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns VERSION_INCREMENT

        every { projectActions.getVersion() } returns SNAPSHOT_001

        every { releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT) } returns RELEASE_001

        allProjectFiles.forEach {
            every { scmActions.getCurrentBranch(it) } returns ScmConfig.GIT_FLOW.featureBranch
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch) }
            justRun { scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch) }
            justRun { projectActions.setVersion(RELEASE_001) }
        }

        underTest = SetReleaseVersionAction(
            createProjectContext(projectActions),
            createReleaseActionContext(),
            releaseVersionProvider,
            scmActions,
            ScmConfig.GIT_FLOW,
            versionIncrementProvider,
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            scmActions.getLastTag(projectFile)
            scmActions.getCommits(projectFile, GIT_TAG)
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            projectActions.getVersion()
            releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)

            subProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it)
                versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
                projectActions.getVersion()
                releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)
            }

            allProjectFiles.forEach {
                scmActions.getCurrentBranch(it)
                scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch)
                scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch)
                projectActions.setVersion(RELEASE_001)
            }
        }
    }

    @Test
    fun test_execute_withTrunkBasedFlow() {
        // GIVEN
        every { scmActions.getLastTag(projectFile) } returns GIT_TAG
        every { scmActions.getCommits(projectFile, GIT_TAG) } returns COMMITS

        subProjectFiles.forEach {
            every { scmActions.getLastTag(it) } throws ScmActionException(null)
            every { scmActions.getCommits(it, null) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns VERSION_INCREMENT

        every { projectActions.getVersion() } returns SNAPSHOT_001

        every { releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT) } returns RELEASE_001

        justRun { projectActions.setVersion(RELEASE_001) }

        underTest = SetReleaseVersionAction(
            createProjectContext(projectActions),
            createReleaseActionContext(),
            releaseVersionProvider,
            scmActions,
            ScmConfig.TRUNK_BASED_FLOW,
            versionIncrementProvider,
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            scmActions.getLastTag(projectFile)
            scmActions.getCommits(projectFile, GIT_TAG)
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            projectActions.getVersion()
            releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)

            subProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it)
                versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
                projectActions.getVersion()
                releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)
            }

            repeat(allProjectFiles.size) {
                projectActions.setVersion(RELEASE_001)
            }
        }
    }

    @MethodSource("isForceReleaseVersionIncrements")
    @ParameterizedTest
    fun test_execute_shouldRelease_whenIsForceReleaseSet(
        versionIncrement: VersionIncrement?,
        expectedVersionIncrement: VersionIncrement,
    ) {
        // GIVEN
        allProjectFiles.forEach {
            every { scmActions.getLastTag(it) } returns GIT_TAG
            every { scmActions.getCommits(it, GIT_TAG) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns versionIncrement

        every { projectActions.getVersion() } returns SNAPSHOT_001

        every { releaseVersionProvider.provide(SNAPSHOT_001, expectedVersionIncrement) } returns RELEASE_001

        allProjectFiles.forEach {
            every { scmActions.getCurrentBranch(it) } returns ScmConfig.GIT_FLOW.featureBranch
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch) }
            justRun { scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch) }
            justRun { projectActions.setVersion(RELEASE_001) }
        }

        underTest = SetReleaseVersionAction(
            createProjectContext(projectActions),
            createReleaseActionContext(isForceRelease = true),
            releaseVersionProvider,
            scmActions,
            ScmConfig.GIT_FLOW,
            versionIncrementProvider,
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            allProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it, GIT_TAG)
                versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
                projectActions.getVersion()
                releaseVersionProvider.provide(SNAPSHOT_001, expectedVersionIncrement)
            }

            allProjectFiles.forEach {
                scmActions.getCurrentBranch(it)
                scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch)
                scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch)
                projectActions.setVersion(RELEASE_001)
            }
        }
    }

    @MethodSource("invalidVersionIncrements")
    @ParameterizedTest
    fun test_execute_shouldFail_whenVersionIncrementIsInvalid(versionIncrement: VersionIncrement?) {
        // GIVEN
        allProjectFiles.forEach {
            every { scmActions.getLastTag(it) } returns GIT_TAG
            every { scmActions.getCommits(it, GIT_TAG) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns versionIncrement

        every { projectActions.getVersion() } returns SNAPSHOT_001

        underTest = SetReleaseVersionAction(
            createProjectContext(projectActions),
            createReleaseActionContext(),
            releaseVersionProvider,
            scmActions,
            ScmConfig.GIT_FLOW,
            versionIncrementProvider,
        )

        // WHEN
        assertThatThrownBy { underTest.execute() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("There are no acceptable commits.")

        // THEN
        verifySequence {
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            allProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it, GIT_TAG)
                versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            }
        }
    }

    companion object {
        private val COMMITS = listOf("fix: message", "test: message")
        private const val GIT_TAG = "0.0.0"
        private val VERSION_INCREMENT = VersionIncrement.PATCH

        @JvmStatic
        private fun invalidVersionIncrements() = listOf(
            Arguments.of(null),
            Arguments.of(VersionIncrement.NONE)
        )

        @JvmStatic
        private fun isForceReleaseVersionIncrements() = listOf(
            Arguments.of(null, VersionIncrement.PATCH),
            Arguments.of(VersionIncrement.NONE, VersionIncrement.PATCH),
            Arguments.of(VersionIncrement.PATCH, VersionIncrement.PATCH),
            Arguments.of(VersionIncrement.MINOR, VersionIncrement.MINOR),
            Arguments.of(VersionIncrement.MAJOR, VersionIncrement.MAJOR)
        )
    }

}
