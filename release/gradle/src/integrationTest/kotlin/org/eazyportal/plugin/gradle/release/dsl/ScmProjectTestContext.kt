package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestContext
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

interface ScmProjectTestContext : TestContext {

    val scmActions: TestScmActions<File>

    val scmConfig: ScmConfig

    val projectActionsMap: MutableMap<String, ProjectActions<File>>

    val projectDir: ProjectDir

}
