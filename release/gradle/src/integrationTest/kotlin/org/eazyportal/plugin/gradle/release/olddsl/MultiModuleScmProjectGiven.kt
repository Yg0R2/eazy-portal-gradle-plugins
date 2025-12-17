package org.eazyportal.plugin.gradle.release.olddsl

import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File

class MultiModuleScmProjectGiven(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    val submoduleProjectDirs: List<ProjectDir>,
    initializeProjectBlock: (() -> Unit) -> Unit,
) : ScmProjectGiven(
    scmActions,
    scmConfig,
    initializeProjectBlock,
) {

    override fun setProjectVersion(
        projectFile: ProjectFile<File>,
        version: Version,
    ) {
        projectActionsMap.computeIfAbsent(projectFile.getFile().absolutePath) {
            GradleProjectActions(projectFile)
        }.setVersion(version)

//        submoduleProjectDirs.forEach {
//            projectActionsMap.computeIfAbsent(it..absolutePath) {
//                GradleProjectActions(projectFile)
//            }.setVersion(version)
//        }
    }

}
