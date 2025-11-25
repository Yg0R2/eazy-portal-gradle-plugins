package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class SingleProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ProjectBaseIntegrationTest(scmUtils) {

    @BeforeEach
    override fun setUpRepositories(@TempDir tempDir: File) {
        super.setUpRepositories(tempDir)

        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmUtils.initializeRepository(remoteProjectDir)
        scmUtils.clone(remoteProjectDir, projectDir)
    }

}

abstract class MultiProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ProjectBaseIntegrationTest(scmUtils) {

}

abstract class ProjectBaseIntegrationTest(
    protected open val scmUtils: ScmUtils,
) {

    protected val projectDir: File
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

    protected val remoteProjectDir: File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/$PROJECT_NAME")
            .also { it.mkdirs() }

    protected lateinit var workingDir: File

    private val projectActionsMap = mutableMapOf<String, ProjectActions<File>>()

    @BeforeEach
    open fun setUpRepositories(@TempDir tempDir: File) {
        workingDir = tempDir
    }

    protected fun getProjectVersion(projectDir: File): Version =
        projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.getVersion()

}
