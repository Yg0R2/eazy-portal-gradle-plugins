package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApplyEazyReleasePluginIntegrationTest : BaseReleaseGradlePluginIntegrationTest() {

    @BeforeEach
    fun setUp() {
        GradleProjectBuilder(
            projectDir = projectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()
    }

    @Test
    fun `apply plugin`() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, "tasks")
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "Eazy-release tasks",
                FINALIZE_RELEASE_VERSION_TASK_NAME,
                FINALIZE_SNAPSHOT_VERSION_TASK_NAME,
                PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME,
                RELEASE_TASK_NAME,
                SET_RELEASE_VERSION_TASK_NAME,
                SET_SNAPSHOT_VERSION_TASK_NAME,
                UPDATE_SCM_TASK_NAME,
            )
    }

    @Test
    fun `apply plugin on subproject should fail`() {
        // GIVEN
        val subprojectDir = projectDir.resolve("subproject")
            .also { it.mkdirs() }

        projectDir.resolve("build.gradle.kts")
            .renameTo(subprojectDir.resolve("build.gradle.kts"))

        projectDir.copyIntoFromResources(this::class.java.simpleName, "settings.gradle.kts.withSubproject")
            .renameTo(projectDir.resolve("settings.gradle.kts"))

        // WHEN
        val actual = createGradleRunner(projectDir, "tasks")
            .buildAndFail()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "An exception occurred applying plugin request [id: 'org.eazyportal.plugin.gradle.release-gradle']",
                "> Failed to apply plugin 'org.eazyportal.plugin.gradle.release-gradle'.",
                "   > Plugin can be applied only to the root project.",
            )
    }

}
