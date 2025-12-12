package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import kotlin.io.path.absolutePathString

abstract class BaseSingleModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig
) : BaseScmProjectTestCase() {

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

abstract class OldBaseSingleModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : OldBaseScmProjectTestCase(scmActions, scmConfig) {

    override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectFile.getPath().absolutePathString()) {
            GradleProjectActions(projectFile)
        }.setVersion(version)
    }

}
