package org.eazyportal.plugin.gradle.portal.settings

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.portal.common.BaseIntegrationTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ApplyEazyPortalSettingsPluginIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        projectDir.initializeGradleProject()
    }

    @Test
    fun test_applyPlugin() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, "listPlugins")
            .build()

        // THEN
        assertThat(actual.output.lines()).contains("org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin")
    }

}
