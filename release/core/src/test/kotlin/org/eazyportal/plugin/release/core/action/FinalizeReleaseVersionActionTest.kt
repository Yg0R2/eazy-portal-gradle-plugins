package org.eazyportal.plugin.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verifySequence
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class FinalizeReleaseVersionActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActions: ProjectActions<File>

    @MockK
    private lateinit var scmActions: ScmActions<File>

    private lateinit var underTest: FinalizeReleaseVersionAction<File>

    @BeforeEach
    fun setUp() {
        underTest = FinalizeReleaseVersionAction(createProjectContext(projectActions), scmActions)
    }

    @Test
    fun test_execute() {
        // GIVEN
        every { projectActions.getVersion() } returns RELEASE_001
        every { projectActions.scmFilesToCommit() } returns FILES_TO_COMMIT
        justRun { scmActions.add(any(), *FILES_TO_COMMIT) }
        justRun { scmActions.commit(any(), "Release version: $RELEASE_001") }
        justRun { scmActions.tag(any(), RELEASE_001) }

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
                scmActions.commit(it, "Release version: $RELEASE_001")
                scmActions.tag(it, RELEASE_001)
            }
        }
    }

}
