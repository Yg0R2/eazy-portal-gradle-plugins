package org.eazyportal.plugin.release.core.action

import io.mockk.every
import io.mockk.justRun
import io.mockk.verifySequence
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class UpdateScmActionTest : ReleaseActionBaseTest() {

    private lateinit var underTest: UpdateScmAction<File>

    @BeforeEach
    fun setUp() {
        underTest = UpdateScmAction(projectFile, releaseActionContext)
    }

    @MethodSource("scmConfigs")
    @ParameterizedTest
    fun test_execute(scmConfig: ScmConfig) {
        // GIVEN
        every { releaseActionContext.scmConfigProvider() } returns scmConfig

        justRun { scmActions.push(any(), scmConfig.remote, scmConfig.releaseBranch, scmConfig.featureBranch) }

        // WHEN
        underTest.execute()

        // THEN
        verifySequence {
            scmActions.getSubmodules(projectFile)
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
