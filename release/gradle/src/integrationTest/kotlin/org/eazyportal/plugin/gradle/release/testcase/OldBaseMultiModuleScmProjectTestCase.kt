package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.gradle.release.testcase.dsl.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import kotlin.io.path.absolutePathString

abstract class BaseMultiModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : BaseScmProjectTestCase(), MultiModuleScmProjectTestCase {

    override lateinit var submoduleProjectDirs: List<ProjectDir>

    final override fun initializeScmProject(workingDir: File) {
        // Initialize root project
        initializeGradleProject(projectDir.remoteDir, PROJECT_NAME)

        scmActions.initializeRepository(projectDir.remoteProjectFile, scmConfig.releaseBranch)

        // Initialize submodules
        submoduleProjectDirs = SUBMODULE_NAMES.map {
            ProjectDir(
                localDir = projectDir.localDir.resolve(it),
                remoteDir = projectDir.remoteDir.resolve(it)
                    .also(File::mkdirs),
            )
        }

        submoduleProjectDirs.forEach {
            initializeGradleProject(it.remoteDir, it.localDir.name)

            scmActions.initializeRepository(it.remoteProjectFile, scmConfig.releaseBranch)

            scmActions.addSubmodule(projectDir.remoteProjectFile, it.remoteProjectFile)

            if (scmConfig.featureBranch != scmConfig.releaseBranch) {
                // Create remote feature branch in submodule
                scmActions.execute(it.remoteProjectFile, "branch", scmConfig.featureBranch)
            }
        }

        scmActions.commit(projectDir.remoteProjectFile, CHORE_ADD_SUBMODULES_COMMIT_MESSAGE)

        if (scmConfig.featureBranch != scmConfig.releaseBranch) {
            // Create remote feature branch in root project
            scmActions.execute(projectDir.remoteProjectFile, "branch", scmConfig.featureBranch)
        }

        scmActions.clone(projectDir.remoteProjectFile, projectDir.localProjectFile)

        if (scmConfig.remote != "origin") {
            // Rename remote
            scmActions.execute(projectDir.localProjectFile, "remote", "rename", "origin", scmConfig.remote)

            submoduleProjectDirs.forEach {
                scmActions.execute(it.localProjectFile, "remote", "rename", "origin", scmConfig.remote)
            }
        }

        if (scmConfig.featureBranch != scmConfig.releaseBranch) {
            // Create local feature branch
            scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
            scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

            submoduleProjectDirs.forEach {
                scmActions.checkout(it.localProjectFile, scmConfig.featureBranch)
                scmActions.checkout(it.localProjectFile, scmConfig.releaseBranch)
            }
        }
    }

    final override fun getProjectVersion(projectFile: ProjectFile<File>): Version =
        projectActionsMap.computeIfAbsent(projectFile.getPath().absolutePathString()) {
            GradleProjectActions(projectFile)
        }.getVersion()


    final override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectDir.localDir.absolutePath) {
            GradleProjectActions(FileSystemProjectFile(projectDir.localDir))
        }.setVersion(version)
    }

}

abstract class OldBaseMultiModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : OldBaseScmProjectTestCase(scmActions, scmConfig) {

    val submoduleProjectFiles: List<ProjectFile<File>>
        get() = SUBMODULE_NAMES.asSequence()
            .map { projectFile.resolve(it) }
            .onEach { it.getFile().mkdirs() }
            .toList()

    val remoteSubmoduleProjectFiles: List<ProjectFile<File>>
        get() = SUBMODULE_NAMES.asSequence()
            .map { remoteProjectFile.resolve(it) }
            .onEach { it.getFile().mkdirs() }
            .toList()

    override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectFile.getPath().absolutePathString()) {
            GradleProjectActions(projectFile)
        }.setVersion(version)

        submoduleProjectFiles.forEach { submoduleProjectFile ->
            projectActionsMap.computeIfAbsent(submoduleProjectFile.getPath().absolutePathString()) {
                GradleProjectActions(submoduleProjectFile)
            }.setVersion(version)
        }
    }

}
