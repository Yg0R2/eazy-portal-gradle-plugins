package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import java.io.File

abstract class SingleModuleScmProjectBaseIntegrationTest1(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : ScmProjectBaseIntegrationTest1(scmActions, scmConfig) {

    @BeforeEach
    fun setUpRepositories() {
        initializeRepository(remoteProjectFile)

        setUpBeforeClone()

        scmActions.clone(remoteProjectFile, projectFile)

        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)
    }

}

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
//
//        // Initialize both branch locally
//        scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
//        scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
    }

    override fun setupRemoteBeforeClone() {
        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmUtils.initializeRepository(remoteProjectDir)
    }

}
