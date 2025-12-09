package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import kotlin.io.path.absolutePathString

abstract class BaseSingleModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    override val workingDir: File,
) : BaseScmProjectTestCase(scmActions, scmConfig, workingDir) {

    override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectFile.getPath().absolutePathString()) {
            GradleProjectActions(projectFile)
        }.setVersion(version)
    }

}
