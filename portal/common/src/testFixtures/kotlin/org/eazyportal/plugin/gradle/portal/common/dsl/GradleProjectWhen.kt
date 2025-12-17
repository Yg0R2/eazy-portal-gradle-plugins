package org.eazyportal.plugin.gradle.portal.common.dsl

import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.eazyportal.plugin.gradle.portal.common.GradleUtils
import org.gradle.testkit.runner.BuildResult
import java.io.File

class GradleProjectWhen(
    val workingDir: File,
) : When {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        GradleUtils.createGradleRunner(workingDir, taskName, *args)
            .build()

    fun gradleTaskFails(taskName: String, vararg args: String): BuildResult =
        GradleUtils.createGradleRunner(workingDir, taskName, *args)
            .buildAndFail()

}
