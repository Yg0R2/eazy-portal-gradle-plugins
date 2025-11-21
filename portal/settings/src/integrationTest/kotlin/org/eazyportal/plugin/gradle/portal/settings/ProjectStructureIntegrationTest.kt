package org.eazyportal.plugin.gradle.portal.settings

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.CommonTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.portal.common.BaseIntegrationTest
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

    companion object {
        private val SUBPROJECT_NAMES = listOf(
            "dummy-api",
            "dummy-application",
            "dummy-behemoth",
            "dummy-client",
            "dummy-common",
            "dummy-dao",
            "dummy-service",
            "dummy-web",
        )
    }

}
