package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.gradle.testkit.runner.BuildResult

open class GradleProjectWhen(
    private val context: GradleProjectContext,
) : When<GradleProjectContext>(context) {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(context.workingDir, taskName, *args)
            .build()

    fun taskFails(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(context.workingDir, taskName, *args)
            .buildAndFail()

}
