package org.eazyportal.plugin.gradle.release.olddsl

import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

data class SingleModuleScmProjectTestContext(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    override val projectDir: ProjectDir,
) : ScmProjectTestContext {

    override val projectActionsMap: MutableMap<String, ProjectActions<File>> = mutableMapOf()

}
