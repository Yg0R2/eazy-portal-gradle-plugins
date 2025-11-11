package org.eazyportal.plugin.gradle.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verifySequence
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.gradle.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.gradle.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.gradle.release.core.project.ProjectActions
import org.eazyportal.plugin.gradle.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.gradle.release.core.scm.exception.ScmActionException
import org.eazyportal.plugin.gradle.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.gradle.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.gradle.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.gradle.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.gradle.release.core.version.model.VersionIncrement
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class SetReleaseVersionActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActionsFactory: ProjectActionsFactory<File>

    @MockK
    private lateinit var releaseVersionProvider: ReleaseVersionProvider

    @MockK
    private lateinit var versionIncrementProvider: VersionIncrementProvider

    private lateinit var underTest: SetReleaseVersionAction<File>

    @BeforeEach
    fun setUp() {
        underTest = SetReleaseVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
            releaseVersionProvider,
            versionIncrementProvider,
        )
    }

    @Test
    fun test_execute_withGitFlow() {
        // GIVEN
        every { releaseActionContext.scmConfigProvider() } returns ScmConfig.GIT_FLOW

        every { scmActions.getLastTag(projectFile) } returns GIT_TAG
        every { scmActions.getCommits(projectFile, GIT_TAG) } returns COMMITS

        subModuleProjectFiles.forEach {
            every { scmActions.getLastTag(it) } throws ScmActionException(null)
            every { scmActions.getCommits(it, null) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns VERSION_INCREMENT

        val projectActions: ProjectActions<File> = mockk {
            every { getVersion() } returns SNAPSHOT_001
            justRun { setVersion(RELEASE_001) }
        }

        every { projectActionsFactory.create(any()) } returns projectActions

        every { releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT) } returns RELEASE_001

        allProjectFiles.forEach {
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch) }
            justRun { scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch) }
        }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            scmActions.getSubmodules(projectFile)
            scmActions.getLastTag(projectFile)
            scmActions.getCommits(projectFile, GIT_TAG)
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            projectActionsFactory.create(projectFile)
            projectActions.getVersion()
            releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)
            subModuleProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it, null)
            }
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            subModuleProjectFiles.forEach {
                projectActionsFactory.create(it)
                projectActions.getVersion()
            }
            releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)
            allProjectFiles.forEach {
                scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch)
                scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch)
                projectActionsFactory.create(it)
                projectActions.setVersion(RELEASE_001)
            }
        }
    }

    @Test
    fun test_execute_withTrunkBasedFlow() {
        // GIVEN
        every { releaseActionContext.scmConfigProvider() } returns ScmConfig.TRUNK_BASED_FLOW

        every { scmActions.getLastTag(projectFile) } returns GIT_TAG
        every { scmActions.getCommits(projectFile, GIT_TAG) } returns COMMITS

        subModuleProjectFiles.forEach {
            every { scmActions.getLastTag(it) } throws ScmActionException(null)
            every { scmActions.getCommits(it, null) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns VERSION_INCREMENT

        val projectActions: ProjectActions<File> = mockk {
            every { getVersion() } returns SNAPSHOT_001
            justRun { setVersion(RELEASE_001) }
        }

        every { projectActionsFactory.create(any()) } returns projectActions

        every { releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT) } returns RELEASE_001

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            scmActions.getSubmodules(projectFile)
            scmActions.getLastTag(projectFile)
            scmActions.getCommits(projectFile, GIT_TAG)
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            projectActionsFactory.create(projectFile)
            projectActions.getVersion()
            releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)
            subModuleProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it, null)
            }
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            subModuleProjectFiles.forEach {
                projectActionsFactory.create(it)
                projectActions.getVersion()
            }
            releaseVersionProvider.provide(SNAPSHOT_001, VERSION_INCREMENT)
            allProjectFiles.forEach {
                projectActionsFactory.create(it)
                projectActions.setVersion(RELEASE_001)
            }
        }
    }

    @MethodSource("isForceReleaseVersionIncrements")
    @ParameterizedTest
    fun test_execute_shouldRelease_whenIsForceReleaseSet(
        versionIncrement: VersionIncrement?,
        expectedVersionIncrement: VersionIncrement
    ) {
        // GIVEN
        every { releaseActionContext.isForceReleaseProvider() } returns true

        every { scmActions.getLastTag(projectFile) } returns GIT_TAG
        every { scmActions.getCommits(projectFile, GIT_TAG) } returns COMMITS

        subModuleProjectFiles.forEach {
            every { scmActions.getLastTag(it) } throws ScmActionException(null)
            every { scmActions.getCommits(it, null) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns versionIncrement

        val projectActions: ProjectActions<File> = mockk {
            every { getVersion() } returns SNAPSHOT_001
            justRun { setVersion(RELEASE_001) }
        }

        every { projectActionsFactory.create(any()) } returns projectActions

        every { releaseVersionProvider.provide(SNAPSHOT_001, expectedVersionIncrement) } returns RELEASE_001

        allProjectFiles.forEach {
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch) }
            justRun { scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch) }
        }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            scmActions.getSubmodules(projectFile)
            scmActions.getLastTag(projectFile)
            scmActions.getCommits(projectFile, GIT_TAG)
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            projectActionsFactory.create(projectFile)
            projectActions.getVersion()
            releaseVersionProvider.provide(SNAPSHOT_001, expectedVersionIncrement)
            subModuleProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it, null)
            }
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            subModuleProjectFiles.forEach {
                projectActionsFactory.create(it)
                projectActions.getVersion()
            }
            releaseVersionProvider.provide(SNAPSHOT_001, expectedVersionIncrement)
            allProjectFiles.forEach {
                scmActions.checkout(it, ScmConfig.GIT_FLOW.releaseBranch)
                scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.featureBranch)
                projectActionsFactory.create(it)
                projectActions.setVersion(RELEASE_001)
            }
        }
    }

    @MethodSource("invalidVersionIncrements")
    @ParameterizedTest
    fun test_execute_shouldFail_whenVersionIncrementIsInvalid(versionIncrement: VersionIncrement?) {
        // GIVEN
        every { scmActions.getLastTag(projectFile) } returns GIT_TAG
        every { scmActions.getCommits(projectFile, GIT_TAG) } returns COMMITS

        subModuleProjectFiles.forEach {
            every { scmActions.getLastTag(it) } throws ScmActionException(null)
            every { scmActions.getCommits(it, null) } returns COMMITS
        }

        every {
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
        } returns versionIncrement

        val projectActions: ProjectActions<File> = mockk {
            every { getVersion() } returns SNAPSHOT_001
        }

        every { projectActionsFactory.create(any()) } returns projectActions

        // WHEN
        assertThatThrownBy { underTest.execute() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("There are no acceptable commits.")

        // THEN
        verifySequence {
            scmActions.getSubmodules(projectFile)
            scmActions.getLastTag(projectFile)
            scmActions.getCommits(projectFile, GIT_TAG)
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
            subModuleProjectFiles.forEach {
                scmActions.getLastTag(it)
                scmActions.getCommits(it, null)
            }
            versionIncrementProvider.provide(COMMITS, ConventionalCommitType.DEFAULT_TYPES)
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
