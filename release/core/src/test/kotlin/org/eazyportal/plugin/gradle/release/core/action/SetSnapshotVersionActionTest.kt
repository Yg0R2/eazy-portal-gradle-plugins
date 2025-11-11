package org.eazyportal.plugin.gradle.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verifySequence
import org.eazyportal.plugin.gradle.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.gradle.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.gradle.release.core.project.ProjectActions
import org.eazyportal.plugin.gradle.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.gradle.release.core.scm.ScmConstants.MAIN_BRANCH
import org.eazyportal.plugin.gradle.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.gradle.release.core.version.SnapshotVersionProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class SetSnapshotVersionActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActionsFactory: ProjectActionsFactory<File>

    @MockK
    private lateinit var snapshotVersionProvider: SnapshotVersionProvider

    private lateinit var underTest: SetSnapshotVersionAction<File>

    @BeforeEach
    fun setUp() {
        underTest = SetSnapshotVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
            snapshotVersionProvider,
        )
    }

    @Test
    fun test_execute_withGitFlow() {
        // GIVEN
        every { releaseActionContext.scmConfigProvider() } returns ScmConfig.GIT_FLOW

        val projectActions: ProjectActions<File> = mockk {
            every { getVersion() } returns RELEASE_001
            justRun { setVersion(SNAPSHOT_002) }
        }

        every { projectActionsFactory.create(any()) } returns projectActions

        every { snapshotVersionProvider.provide(RELEASE_001) } returns SNAPSHOT_002

        allProjectFiles.forEach {
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.featureBranch) }
            justRun { scmActions.mergeNoCommit(it, MAIN_BRANCH) }
        }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            projectActionsFactory.create(projectFile)
            projectActions.getVersion()
            snapshotVersionProvider.provide(RELEASE_001)
            scmActions.getSubmodules(projectFile)
            allProjectFiles.forEach {
                scmActions.checkout(it, ScmConfig.GIT_FLOW.featureBranch)
                scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.releaseBranch)

                projectActionsFactory.create(it)
                projectActions.setVersion(SNAPSHOT_002)
            }
        }
    }

    @Test
    fun test_execute_withTrunkBasedFlow() {
        // GIVEN
        every { releaseActionContext.scmConfigProvider() } returns ScmConfig.TRUNK_BASED_FLOW

        val projectActions: ProjectActions<File> = mockk {
            every { getVersion() } returns RELEASE_001
            justRun { setVersion(SNAPSHOT_002) }
        }

        every { projectActionsFactory.create(any()) } returns projectActions

        every { snapshotVersionProvider.provide(RELEASE_001) } returns SNAPSHOT_002

        allProjectFiles.forEach {
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.featureBranch) }
            justRun { scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.releaseBranch) }
        }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            projectActionsFactory.create(projectFile)
            projectActions.getVersion()
            snapshotVersionProvider.provide(RELEASE_001)
            scmActions.getSubmodules(projectFile)
            allProjectFiles.forEach {
                projectActionsFactory.create(it)
                projectActions.setVersion(SNAPSHOT_002)
            }
        }
    }

}
