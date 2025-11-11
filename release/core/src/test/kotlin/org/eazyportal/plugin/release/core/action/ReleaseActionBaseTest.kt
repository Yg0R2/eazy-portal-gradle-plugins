package org.eazyportal.plugin.release.core.action

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File

@ExtendWith(MockKExtension::class)
abstract class ReleaseActionBaseTest {

    protected lateinit var allProjectFiles: Set<ProjectFile<File>>

    protected lateinit var projectFile: ProjectFile<File>

    @MockK
    protected lateinit var releaseActionContext: ReleaseActionContext<File>

    @MockK
    protected lateinit var scmActions: ScmActions<File>

    protected lateinit var subModuleProjectFiles: Set<ProjectFile<File>>

    @BeforeEach
    fun setUp(@TempDir workingDir: File) {
        projectFile = FileSystemProjectFile(workingDir)
        subModuleProjectFiles = setOf(projectFile.resolve(SUBMODULE_NAME))
        allProjectFiles = setOf(projectFile) + subModuleProjectFiles

        every { releaseActionContext.conventionalCommitTypesProvider } returns { ConventionalCommitType.DEFAULT_TYPES }
        every { releaseActionContext.isForceReleaseProvider } returns { false }
        every { releaseActionContext.scmActionsProvider } returns { scmActions }
        every { releaseActionContext.scmConfigProvider } returns { ScmConfig.GIT_FLOW }

        every { scmActions.getSubmodules(projectFile) } returns listOf(SUBMODULE_NAME)
    }

    companion object {
        protected const val FILE_TO_COMMIT = "."
        protected const val SUBMODULE_NAME = "ui"
    }

}
