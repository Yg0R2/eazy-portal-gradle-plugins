package org.eazyportal.plugin.gradle.portal.settings

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ProjectStructureIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        projectDir.initializeGradleProject(*SUBPROJECT_NAMES.toTypedArray())
    }

    @Test
    fun test_applyPlugin() {
        // GIVEN
        // WHEN
        // THEN
        val actual = createGradleRunner(projectDir, "projects", "-Pversion=0.0.1-SNAPSHOT")
            .build()

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

    companion object {
        private val SUBPROJECT_NAMES = listOf(
            "dummy-api",
            "dummy-behemoth",
            "dummy-client",
            "dummy-common",
            "dummy-dao",
            "dummy-service",
            "dummy-web",
        )
    }

}
