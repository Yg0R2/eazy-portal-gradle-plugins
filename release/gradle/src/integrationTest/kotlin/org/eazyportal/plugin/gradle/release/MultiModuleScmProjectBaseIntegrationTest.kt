package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import java.io.File

abstract class MultiModuleScmProjectBaseIntegrationTest1(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : ScmProjectBaseIntegrationTest1(scmActions, scmConfig) {

    protected val remoteSubmoduleProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$SUBMODULE_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

    protected val submoduleProjectFile: ProjectFile<File>
        get() = projectFile.resolve(SUBMODULE_NAME)
            .also { it.getFile().mkdirs() }

    @BeforeEach
    fun setUpRepositories() {
        initializeRepository(remoteProjectFile)

        initializeRepository(remoteSubmoduleProjectFile)

        scmActions.addSubmodule(remoteProjectFile, remoteSubmoduleProjectFile)
        scmActions.commit(remoteProjectFile, "chore: add $SUBMODULE_NAME submodule")

        setUpBeforeClone()

        scmActions.clone(remoteProjectFile, projectFile)

        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)
    }

}

abstract class MultiModuleScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ScmProjectBaseIntegrationTest(scmUtils) {

    val subModuleDir: File
        get() = projectDir.resolve(SUBMODULE_NAME)
            .also { it.mkdirs() }

    val remoteSubModuleDir : File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/$SUBMODULE_NAME")
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
//
//        // Initialize both branch locally
//        scmUtils.checkout(remoteProjectDir, ScmConstants.FEATURE_BRANCH)
//        scmUtils.checkout(remoteProjectDir, ScmConstants.RELEASE_BRANCH)
    }

    override fun setupRemoteBeforeClone() {
        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()
        scmUtils.initializeRepository(remoteProjectDir)

        GradleProjectBuilder(
            projectDir = remoteSubModuleDir,
            projectPluginIds = setOf("java")
        ).build()
        scmUtils.initializeRepository(remoteSubModuleDir)

        scmUtils.addSubmodule(remoteProjectDir, remoteSubModuleDir)
        scmUtils.commit(remoteProjectDir, "chore: add $SUBMODULE_NAME submodule")
    }

}
