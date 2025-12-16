package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.dsl.given.GivenContext
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

interface ScmProjectGivenContext : GivenContext {

    val projectDir: ProjectDir

}

open class SingleModuleScmProjectGivenContext(
    override val projectDir: ProjectDir,
    val scmActions: TestScmActions<File>,
    val scmConfig: ScmConfig,
) : ScmProjectGivenContext {

    fun withGradleProject(block: GradleProjectBuilder.() -> Unit) {
        GradleProjectBuilder(projectDir.remoteDir)
            .block()
    }

    fun withScmProject(block: ScmProjectBuilder.() -> Unit) {
        ScmProjectBuilder()
            .apply(block)
            .build()
    }

}