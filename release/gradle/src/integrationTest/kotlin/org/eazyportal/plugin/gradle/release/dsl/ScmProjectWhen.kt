package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.gradle.testkit.runner.BuildResult

class ScmProjectWhen<out C : ScmProjectTestContext>(
    private val context: C,
) : When<C>(context) {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(context.projectDir.localDir, taskName, *args)
            .build()

    fun gradleTaskFails(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(context.projectDir.localDir, taskName, *args)
            .buildAndFail()

}
