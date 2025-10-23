package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ApplyPluginIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        projectDir.initializeGradleProject()
    }

    @Test
    fun test_applyPlugin() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, "tasks")
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "Eazy-release tasks",
                "setReleaseVersion",
            )
    }

}
