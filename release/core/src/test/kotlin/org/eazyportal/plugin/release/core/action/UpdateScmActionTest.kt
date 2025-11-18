package org.eazyportal.plugin.release.core.action

import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verifySequence
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class UpdateScmActionTest : ReleaseActionBaseTest() {

    @MockK
    private lateinit var projectActions: ProjectActions<File>

    @MockK
    private lateinit var scmActions: ScmActions<File>

    private lateinit var underTest: UpdateScmAction<File>

    @MethodSource("scmConfigs")
    @ParameterizedTest
    fun test_execute(scmConfig: ScmConfig) {
        // GIVEN
        justRun { scmActions.push(any(), scmConfig.remote, scmConfig.releaseBranch, scmConfig.featureBranch) }

        underTest = UpdateScmAction(
            createProjectContext(projectActions),
            scmActions,
            scmConfig,
        )

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            repeat(allProjectFiles.size) {
                projectActions.hashCode() // because of createProjectContext
            }

            allProjectFiles.forEach {
                scmActions.push(it, scmConfig.remote, scmConfig.releaseBranch, scmConfig.featureBranch)
            }
        }
    }

    companion object {
        @JvmStatic
        fun scmConfigs() = listOf(
            Arguments.of(ScmConfig.GIT_FLOW),
            Arguments.of(ScmConfig.TRUNK_BASED_FLOW),
            Arguments.of(ScmConfig("feature-branch", "release-branch", "remote-repository"))
        )
    }

}
