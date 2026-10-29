package org.eazyportal.gradle.eazyproject.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class ProjectTypeTest {

    @TestFactory
    fun `fromProjectName resolves the standard kebab names, case-insensitively`(): List<DynamicTest> =
        listOf(
            "service" to ProjectType.SERVICE,
            "Api" to ProjectType.API,
            "APPLICATION" to ProjectType.APPLICATION,
            "common" to ProjectType.COMMON,
            "Persistence" to ProjectType.PERSISTENCE,
            "CLIENT" to ProjectType.CLIENT,
            "web" to ProjectType.WEB,
            "default" to ProjectType.DEFAULT,
        ).map { (name, expected) ->
            dynamicTest("\"$name\" to ${expected.name}") {
                assertThat(ProjectType.fromProjectName(name))
                    .isEqualTo(expected)
            }
        }

    @TestFactory
    fun `fromProjectName resolves the kebab name suffix, case-insensitively`(): List<DynamicTest> =
        listOf(
            "example-service" to ProjectType.SERVICE,
            "payment-Api" to ProjectType.API,
            "billing-APPLICATION" to ProjectType.APPLICATION,
            "shared-common" to ProjectType.COMMON,
            "user-Persistence" to ProjectType.PERSISTENCE,
            "admin-CLIENT" to ProjectType.CLIENT,
            "portal-web" to ProjectType.WEB,
            "fallback-default" to ProjectType.DEFAULT,
        ).map { (name, expected) ->
            dynamicTest("\"$name\" to ${expected.name}") {
                assertThat(ProjectType.fromProjectName(name))
                    .isEqualTo(expected)
            }
        }

    @TestFactory
    fun `only application, persistence, service and web are framework-specific`(): List<DynamicTest> =
        ProjectType.entries.map { projectType ->
            val expected = projectType in setOf(
                ProjectType.APPLICATION,
                ProjectType.PERSISTENCE,
                ProjectType.SERVICE,
                ProjectType.WEB,
            )

            dynamicTest("$projectType frameworkSpecific=$expected") {
                assertThat(projectType.isFrameworkSpecific)
                    .isEqualTo(expected)
            }
        }

    @TestFactory
    fun `fromProjectName falls back to DEFAULT for unknown names`(): List<DynamicTest> =
        listOf(
            "paymentAPI",
            "payment-library",
            "custom-module",
            "customService",
        ).map { name ->
            dynamicTest("\"$name\" to DEFAULT") {
                assertThat(ProjectType.fromProjectName(name))
                    .isEqualTo(ProjectType.DEFAULT)
            }
        }

}
