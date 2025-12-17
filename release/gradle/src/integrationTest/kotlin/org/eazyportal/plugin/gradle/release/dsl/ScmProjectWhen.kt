package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.eazyportal.plugin.gradle.portal.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.gradle.testkit.runner.BuildResult

class ScmProjectWhen(
    val projectDir: ProjectDir,
) : When {

    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir.localDir, taskName, *args)
            .build()

    fun gradleTaskFails(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(projectDir.localDir, taskName, *args)
            .buildAndFail()


}
