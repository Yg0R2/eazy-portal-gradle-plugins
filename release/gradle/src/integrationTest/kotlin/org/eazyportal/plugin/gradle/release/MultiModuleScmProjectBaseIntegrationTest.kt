package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import java.io.File

abstract class MultiModuleScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ScmProjectBaseIntegrationTest(scmUtils) {

    val subModuleDir: File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/${GradleTestFixtures.SUBMODULE_NAME}")
            .also { it.mkdirs() }

    final override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.setVersion(version)

        projectActionsMap.computeIfAbsent(subModuleDir.path) {
            GradleProjectActions(FileSystemProjectFile(subModuleDir))
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

        GradleProjectBuilder(
            projectDir = subModuleDir,
            projectPluginIds = setOf("java")
        ).build()
        scmUtils.initializeRepository(subModuleDir)

        scmUtils.addSubmodule(remoteProjectDir, subModuleDir)
        scmUtils.commit(remoteProjectDir, "chore: add ${subModuleDir.name} submodule")
    }

}
