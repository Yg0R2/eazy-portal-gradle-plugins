package org.eazyportal.plugin.release.core.action

import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verifySequence
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.Test
import java.io.File

class PrepareRepositoryForReleaseActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActions: ProjectActions<File>

    @MockK
    private lateinit var scmActions: ScmActions<File>

    private lateinit var underTest: PrepareRepositoryForReleaseAction<File>

    @Test
    fun test_execute_withGitFlow() {
        // GIVEN
        justRun { scmActions.clean(any()) }
        justRun { scmActions.fetch(projectFile, ScmConfig.GIT_FLOW.remote) }
        justRun { scmActions.checkout(any(), ScmConfig.GIT_FLOW.featureBranch) }

        underTest = PrepareRepositoryForReleaseAction(
            createProjectContext(projectActions),
            createReleaseActionContext(scmActions = scmActions),
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

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
        // GIVEN
        justRun { scmActions.clean(any()) }
        justRun { scmActions.fetch(projectFile, ScmConfig.TRUNK_BASED_FLOW.remote) }

        underTest = PrepareRepositoryForReleaseAction(
            createProjectContext(projectActions),
            createReleaseActionContext(
                scmActions = scmActions,
                scmConfig = ScmConfig.TRUNK_BASED_FLOW,
            ),
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            allProjectFiles.forEach {
                scmActions.clean(it)
            }

            scmActions.fetch(projectFile, ScmConfig.TRUNK_BASED_FLOW.remote)
        }
    }

}
