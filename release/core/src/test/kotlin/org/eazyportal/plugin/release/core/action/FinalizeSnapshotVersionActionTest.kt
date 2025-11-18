package org.eazyportal.plugin.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verifySequence
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class FinalizeSnapshotVersionActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActions: ProjectActions<File>

    @MockK
    private lateinit var scmActions: ScmActions<File>

    private lateinit var underTest: FinalizeSnapshotVersionAction<File>

    @BeforeEach
    fun setUp() {
        underTest = FinalizeSnapshotVersionAction(createProjectContext(projectActions), scmActions)
    }

    @Test
    fun test_execute() {
        // GIVEN
        every { projectActions.getVersion() } returns SNAPSHOT_002
        every { projectActions.scmFilesToCommit() } returns FILES_TO_COMMIT
        justRun { scmActions.add(any(), *FILES_TO_COMMIT) }
        justRun { scmActions.commit(any(), "New SNAPSHOT version: $SNAPSHOT_002") }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            projectActions.getVersion()
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            allProjectFiles.reversed().forEach {
                projectActions.scmFilesToCommit()

                scmActions.add(it, *FILES_TO_COMMIT)
                scmActions.commit(it, "New SNAPSHOT version: $SNAPSHOT_002")
            }
        }
    }

}
