package org.eazyportal.plugin.gradle.portal.settings

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.CommonTestFixtures.SUBPROJECT_NAMES
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.gradle.portal.common.BaseIntegrationTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class EazyPortalProjectStructureIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        GradleProjectBuilder(
            projectDir = projectDir,
            settingsPluginIds = setOf("org.eazyportal.plugin.gradle.portal-settings"),
            subProjectNames = SUBPROJECT_NAMES,
        ).withExtraSettingsConfig(
            """
            eazyPortal {
                applyCoreDependencies = false
            }
            """.trimIndent()
        ).withExtraProjectConfig(
            """
            allprojects {
                dependencies {
                    testImplementation(platform("org.junit:junit-bom:+"))
            
                    testImplementation("org.junit.jupiter:junit-jupiter")
                    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
                }
            }
            """.trimIndent()
        ).build()

        SUBPROJECT_NAMES.forEach {
            projectDir.copyIntoFromResources(this::class.java.simpleName, "$it/")
        }
    }

    @Test
    fun test_validateProjectStructure() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, "build")
            .build()

        // THEN
        listOf("", *SUBPROJECT_NAMES.map { ":$it" }.toTypedArray()).forEach {
            assertThat(actual.output).contains(
                "> Task $it:test",
            )
        }
    }

}
