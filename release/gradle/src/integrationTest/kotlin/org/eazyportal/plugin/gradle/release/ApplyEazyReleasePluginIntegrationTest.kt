package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(OrderAnnotation::class)
class ApplyEazyReleasePluginIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        projectDir.initializeGradleProject()
    }

    @Order(0)
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
                SET_RELEASE_VERSION_TASK_NAME,
            )
    }

    @Order(1)
    @Test
    fun `apply plugin on subproject should fail`() {
        // GIVEN
        val subprojectDir = projectDir.resolve("subproject")
            .also { it.mkdirs() }

        projectDir.resolve("build.gradle.kts")
            .renameTo(subprojectDir.resolve("build.gradle.kts"))

        projectDir.copyIntoFromResources("settings.gradle.kts.withSubproject")
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
