package org.eazyportal.plugin.common.integration.test.gradle.dsl

import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils
import org.gradle.testkit.runner.BuildResult

class GradleProjectWhen(
    private val context: GradleProjectTestContext,
) : When<GradleProjectTestContext>(context) {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        GradleUtils.createGradleRunner(context.workingDir, taskName, *args)
            .build()

    fun gradleTaskFails(taskName: String, vararg args: String): BuildResult =
        GradleUtils.createGradleRunner(context.workingDir, taskName, *args)
            .buildAndFail()

}
