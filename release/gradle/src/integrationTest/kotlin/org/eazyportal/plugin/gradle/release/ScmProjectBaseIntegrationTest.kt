package org.eazyportal.plugin.gradle.release

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
) : BaseIntegrationTest(), ScmProjectIntegrationTest {

    override val remoteProjectDir: File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/$PROJECT_NAME")
            .also { it.mkdirs() }

    protected val projectActionsMap = mutableMapOf<String, ProjectActions<File>>()

    final override fun getProjectVersion(projectDir: File, branch: String?): Version {
        if (branch != null) {
            scmUtils.checkout(projectDir, branch)
        }

        return projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.getVersion()
    }

    protected abstract fun setupRemoteBeforeClone()

}
