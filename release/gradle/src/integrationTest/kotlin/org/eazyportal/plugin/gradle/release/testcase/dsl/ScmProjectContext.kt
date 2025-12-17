package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

interface ScmProjectContext : GivenContext, WhenContext, ThenContext {

    val projectActionsMap: MutableMap<String, ProjectActions<out Any>>

    val scmActions: TestScmActions<File>

    val scmConfig: ScmConfig

}
