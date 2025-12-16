package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenContext
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.gradle.testkit.runner.BuildResult
import java.io.File

class GradleProjectWhenContext(
    private val projectDir: File,
) : WhenContext {

    fun taskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir, taskName, *args)
            .build()

    fun taskFails(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir, taskName, *args)
            .buildAndFail()

}
