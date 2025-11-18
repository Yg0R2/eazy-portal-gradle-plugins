package org.eazyportal.plugin.release.core.action

import io.mockk.junit5.MockKExtension
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File

@ExtendWith(MockKExtension::class)
@MockKExtension.ConfirmVerification
abstract class ReleaseActionBaseTest {

    protected lateinit var projectFile: ProjectFile<File>

    protected lateinit var allProjectFiles: Set<ProjectFile<File>>

    protected lateinit var subProjectFiles: Set<ProjectFile<File>>

    @BeforeEach
    fun setUp(@TempDir workingDir: File) {
        projectFile = FileSystemProjectFile(workingDir)

        subProjectFiles = SUBMODULE_NAMES.map { projectFile.resolve(it) }.toSet()
        allProjectFiles = setOf(projectFile) + subProjectFiles
    }

    protected fun createProjectContext(
        projectActions: ProjectActions<File>,
        projectFile: ProjectFile<File> = this.projectFile,
    ): ProjectContext<File> =
        ProjectContext(
            root = ProjectContext.Pair(projectActions, projectFile),
            sub = SUBMODULE_NAMES
                .map { ProjectContext.Pair(projectActions, projectFile.resolve(it)) }
                .toSet()
        )

    protected fun createReleaseActionContext(
        conventionalCommitTypes: List<ConventionalCommitType> = ConventionalCommitType.DEFAULT_TYPES,
        isForceRelease: Boolean = false,
    ): ReleaseActionContext =
        ReleaseActionContext(
            conventionalCommitTypes,
            isForceRelease,
        )

    companion object {
        @JvmStatic
        protected val FILES_TO_COMMIT = arrayOf(".")

        @JvmStatic
        protected val SUBMODULE_NAMES = listOf("ui")
    }

}
