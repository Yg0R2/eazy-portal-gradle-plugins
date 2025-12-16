package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.gradle.testkit.runner.BuildResult
import java.io.File

open class GradleProjectWhen(
    private val projectDir: File,
) : When() {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir, taskName, *args)
            .build()

    fun taskFails(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir, taskName, *args)
            .buildAndFail()

}
