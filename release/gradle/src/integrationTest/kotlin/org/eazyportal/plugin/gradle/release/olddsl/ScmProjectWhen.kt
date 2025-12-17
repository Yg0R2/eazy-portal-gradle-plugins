package org.eazyportal.plugin.gradle.release.olddsl

import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.gradle.testkit.runner.BuildResult
import java.io.File

class ScmProjectWhen : When {

    fun gradleTaskSucceeds(
        projectFile: ProjectFile<File>,
        taskName: String,
        vararg args: String,
    ): BuildResult =
        createGradleRunner(projectFile.getFile(), taskName, *args)
            .build()

    fun gradleTaskFails(
        projectFile: ProjectFile<File>,
        taskName: String,
        vararg args: String,
    ): BuildResult =
        createGradleRunner(projectFile.getFile(), taskName, *args)
            .buildAndFail()

}
