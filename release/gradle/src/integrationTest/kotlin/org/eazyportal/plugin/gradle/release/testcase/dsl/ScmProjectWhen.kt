package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.dsl.When
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.gradle.testkit.runner.BuildResult

class ScmProjectWhen(
    private val projectDir: ProjectDir,
) : When() {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir.localDir, taskName, *args)
            .build()

    fun gradleTaskFails(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir.localDir, taskName, *args)
            .buildAndFail()

}
