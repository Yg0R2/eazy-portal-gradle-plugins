package org.eazyportal.gradle.eazyproject

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
    fun `fromProjectName falls back to DEFAULT for unknown names`(): List<DynamicTest> =
        listOf(
            "payment-service",
            "custom-module",
        ).map { name ->
            dynamicTest("\"$name\" to DEFAULT") {
                assertThat(ProjectType.fromProjectName(name))
                    .isEqualTo(ProjectType.DEFAULT)
            }
        }

}
