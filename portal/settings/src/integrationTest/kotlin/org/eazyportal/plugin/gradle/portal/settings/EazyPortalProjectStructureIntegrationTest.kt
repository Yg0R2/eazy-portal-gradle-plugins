package org.eazyportal.plugin.gradle.portal.settings

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.CommonTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.gradle.portal.common.BaseIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(OrderAnnotation::class)
class EazyPortalProjectStructureIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        projectDir.initializeGradleProject(*EP_SUBPROJECT_NAMES)
        EP_SUBPROJECT_NAMES.forEach {
            projectDir.copyIntoFromResources(this::class.java.simpleName, "$it/")
        }
    }

    @AfterEach
    fun tearDown() {
        createGradleRunner(projectDir, "clean")
            .build()
    }

    @Test
    fun test_validateProjectStructure() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, "build")
            .build()

        // THEN
        listOf("", *EP_SUBPROJECT_NAMES.map { ":$PROJECT_NAME-$it" }.toTypedArray()).forEach {
            assertThat(actual.output).contains(
                "> Task $it:test",
            )
        }
    }

    companion object {
        private val EP_SUBPROJECT_NAMES = arrayOf<String>(
            "api",
            "application",
            "behemoth",
            "client",
            "common",
            "dao",
            "service",
            "web",
        )
    }

}
