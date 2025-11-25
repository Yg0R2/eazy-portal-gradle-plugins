package org.eazyportal.plugin.gradle

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class ScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ScmProjectIntegrationTest {

    override val projectDir: File
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

    override val remoteProjectDir: File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/$PROJECT_NAME")
            .also { it.mkdirs() }

    protected lateinit var workingDir: File

    private val projectActionsMap = mutableMapOf<String, ProjectActions<File>>()

    @BeforeEach
    open fun setUpRepositories(@TempDir tempDir: File) {
        workingDir = tempDir
    }

    final override fun getProjectVersion(projectDir: File, branch: String?): Version {
        if (branch != null) {
            scmUtils.checkout(projectDir, branch)
        }

        return projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.getVersion()
    }

    final override fun setProjectVersion(projectDir: File, version: Version) {
        projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.setVersion(version)
    }

}
