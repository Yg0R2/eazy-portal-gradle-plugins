package org.eazyportal.plugin.gradle.portal.settings

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleTestFixtures.SUBPROJECT_NAMES
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.gradle.portal.common.BaseIntegrationTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ProjectStructureIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        GradleProjectBuilder(
            projectDir = projectDir,
            settingsPluginIds = setOf("org.eazyportal.plugin.gradle.portal-settings"),
            subProjectNames = SUBPROJECT_NAMES
        ).withExtraSettingsConfig(
            """
            eazyPortal {
                applicationType = org.eazyportal.plugin.gradle.portal.common.model.ApplicationTypes.SPRING_BOOT
            }
            """.trimIndent()
        ).build()
    }

    @Test
    fun test_applyPlugin() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, "projects")
            .build()

        // THEN
        val expectedSubprojects = SUBPROJECT_NAMES.withIndex()
            .map{ (index, subprojectName) ->
                if (index < SUBPROJECT_NAMES.size - 1) {
                    "+--- Project ':$subprojectName'"
                } else {
                    "\\--- Project ':$subprojectName'"
                }
            }.toTypedArray()

        assertThat(actual.output.lines()).contains(
            "Root project '$PROJECT_NAME'",
            *expectedSubprojects,
        )
    }

}
