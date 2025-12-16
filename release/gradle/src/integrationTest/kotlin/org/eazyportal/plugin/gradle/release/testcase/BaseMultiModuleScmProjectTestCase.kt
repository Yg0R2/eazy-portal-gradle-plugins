package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.gradle.release.testcase.dsl.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File

abstract class BaseMultiModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : OldBaseScmProjectTestCase(), MultiModuleScmProjectTestCase {

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

    final override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectDir.localDir.absolutePath) {
            GradleProjectActions(FileSystemProjectFile(projectDir.localDir))
        }.setVersion(version)

        submoduleProjectDirs.forEach { submoduleProjectDir ->
            projectActionsMap.computeIfAbsent(submoduleProjectDir.localDir.absolutePath) {
                GradleProjectActions(FileSystemProjectFile(submoduleProjectDir.localDir))
            }.setVersion(version)
        }
    }

//    final override fun setProjectVersion(projectFile: ProjectFile<File>, version: Version) {
//        projectActionsMap.computeIfAbsent(projectFile.getFile().absolutePath) {
//            GradleProjectActions(projectFile)
//        }.setVersion(version)
//    }

}
