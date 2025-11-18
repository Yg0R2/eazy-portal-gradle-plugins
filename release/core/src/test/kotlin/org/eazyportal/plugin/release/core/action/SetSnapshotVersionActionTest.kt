package org.eazyportal.plugin.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verifySequence
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.SnapshotVersionProvider
import org.junit.jupiter.api.Test
import java.io.File

class SetSnapshotVersionActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActions: ProjectActions<File>

    @MockK
    private lateinit var scmActions: ScmActions<File>

    @MockK
    private lateinit var snapshotVersionProvider: SnapshotVersionProvider

    private lateinit var underTest: SetSnapshotVersionAction<File>

    @Test
    fun test_execute_withGitFlow() {
        // GIVEN
        every { projectActions.getVersion() } returns RELEASE_001

        every { snapshotVersionProvider.provide(RELEASE_001) } returns SNAPSHOT_002

        allProjectFiles.forEach {
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.featureBranch) }
            justRun { scmActions.mergeNoCommit(it, RELEASE_BRANCH) }
            justRun { projectActions.setVersion(SNAPSHOT_002) }
        }

        underTest = SetSnapshotVersionAction(
            createProjectContext(projectActions),
            createReleaseActionContext(scmActions = scmActions),
            snapshotVersionProvider,
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            projectActions.getVersion()

            snapshotVersionProvider.provide(RELEASE_001)

            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            allProjectFiles.forEach {
                scmActions.checkout(it, ScmConfig.GIT_FLOW.featureBranch)
                scmActions.mergeNoCommit(it, ScmConfig.GIT_FLOW.releaseBranch)

                projectActions.setVersion(SNAPSHOT_002)
            }
        }
    }

    @Test
    fun test_execute_withTrunkBasedFlow() {
        // GIVEN
        every { projectActions.getVersion() } returns RELEASE_001

        every { snapshotVersionProvider.provide(RELEASE_001) } returns SNAPSHOT_002

        justRun { projectActions.setVersion(SNAPSHOT_002) }

        underTest = SetSnapshotVersionAction(
            createProjectContext(projectActions),
            createReleaseActionContext(
                scmActions = scmActions,
                scmConfig = ScmConfig.TRUNK_BASED_FLOW,
            ),
            snapshotVersionProvider,
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            projectActions.getVersion()
            snapshotVersionProvider.provide(RELEASE_001)

            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            allProjectFiles.forEach {
                projectActions.setVersion(SNAPSHOT_002)
            }
        }
    }

}
