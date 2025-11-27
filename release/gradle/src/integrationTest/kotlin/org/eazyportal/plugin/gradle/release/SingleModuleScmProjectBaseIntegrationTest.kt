package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach

abstract class SingleModuleScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ScmProjectBaseIntegrationTest(scmUtils) {

    final override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.setVersion(version)
    }

    @BeforeEach
    fun setUpRepositories() {
        setupRemoteBeforeClone()

        scmUtils.clone(remoteProjectDir, projectDir)
    }

    override fun setupRemoteBeforeClone() {
        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmUtils.initializeRepository(remoteProjectDir)
    }

}
