package org.eazyportal.plugin.gradle.release.core.action

import io.mockk.every
import io.mockk.justRun
import io.mockk.verifySequence
import org.eazyportal.plugin.gradle.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class PrepareRepositoryForReleaseActionTest : ReleaseActionBaseTest() {

    private lateinit var underTest: PrepareRepositoryForReleaseAction<File>

    @BeforeEach
    fun setUp() {
        underTest = PrepareRepositoryForReleaseAction(projectFile, releaseActionContext)
    }

    @Test
    fun test_execute_withGitFlow() {
        // GIVEN
        every { releaseActionContext.scmConfigProvider() } returns ScmConfig.GIT_FLOW

        allProjectFiles.forEach {
            justRun { scmActions.clean(it) }
        }

        justRun { scmActions.fetch(projectFile, ScmConfig.GIT_FLOW.remote) }

        allProjectFiles.forEach {
            justRun { scmActions.checkout(it, ScmConfig.GIT_FLOW.featureBranch) }
        }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            scmActions.getSubmodules(projectFile)

            allProjectFiles.forEach {
                scmActions.clean(it)
            }

            scmActions.fetch(projectFile, ScmConfig.GIT_FLOW.remote)

            allProjectFiles.forEach {
                scmActions.checkout(it, ScmConfig.GIT_FLOW.featureBranch)
            }
        }
    }

    @Test
    fun test_execute_withTrunkBasedFlow() {
        every { releaseActionContext.scmConfigProvider() } returns ScmConfig.TRUNK_BASED_FLOW

        allProjectFiles.forEach {
            justRun { scmActions.clean(it) }
        }

        justRun { scmActions.fetch(projectFile, ScmConfig.GIT_FLOW.remote) }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            scmActions.getSubmodules(projectFile)

            allProjectFiles.forEach {
                scmActions.clean(it)
            }

            scmActions.fetch(projectFile, ScmConfig.GIT_FLOW.remote)
        }
    }

}
