package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import kotlin.io.path.absolutePathString

abstract class BaseMultiModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : BaseScmProjectTestCase(scmActions, scmConfig) {

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
