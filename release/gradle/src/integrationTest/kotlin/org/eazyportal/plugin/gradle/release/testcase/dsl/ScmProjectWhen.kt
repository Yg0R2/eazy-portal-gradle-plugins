package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.dsl.When
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.gradle.testkit.runner.BuildResult

class ScmProjectWhen(
    private val context: ScmProjectContext,
) : When<ScmProjectContext>(context) {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(context.projectDir.localDir, taskName, *args)
            .build()
            .also(context.executionResult::set)

    fun gradleTaskFails(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(context.projectDir.localDir, taskName, *args)
            .buildAndFail()
            .also(context.executionResult::set)

}
