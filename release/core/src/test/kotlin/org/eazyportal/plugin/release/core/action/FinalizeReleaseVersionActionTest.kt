package org.eazyportal.plugin.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verifySequence
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectActionsFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class FinalizeReleaseVersionActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActionsFactory: ProjectActionsFactory<File>

    private lateinit var underTest: FinalizeReleaseVersionAction<File>

    @BeforeEach
    fun setUp() {
        underTest = FinalizeReleaseVersionAction(projectActionsFactory, projectFile, releaseActionContext)
    }

    @Test
    fun test_execute() {
        // GIVEN
        val scmFilesToCommit = arrayOf(".")

        val projectActions = mockk<ProjectActions<File>> {
            every { getVersion() } returns RELEASE_001
            every { scmFilesToCommit() } returns scmFilesToCommit
        }

        allProjectFiles.forEach {
            every { projectActionsFactory.create(it) } returns projectActions

            justRun { scmActions.add(it, *scmFilesToCommit) }
            justRun { scmActions.commit(it, "Release version: $RELEASE_001") }
            justRun { scmActions.tag(it, RELEASE_001) }
        }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            projectActionsFactory.create(projectFile)

            scmActions.getSubmodules(projectFile)

            allProjectFiles.reversed().forEach {
                projectActionsFactory.create(it)

                scmActions.add(it, *scmFilesToCommit)
                scmActions.commit(it, "Release version: $RELEASE_001")
                scmActions.tag(it, RELEASE_001)
            }
        }
    }

}
