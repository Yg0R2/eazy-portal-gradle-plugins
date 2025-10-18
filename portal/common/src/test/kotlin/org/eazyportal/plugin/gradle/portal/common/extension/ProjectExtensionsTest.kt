package org.eazyportal.plugin.gradle.portal.common.extension

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.gradle.api.Project
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class ProjectExtensionsTest {

    @TestFactory
    fun test_getType(): List<DynamicTest> {
        val rootProject = mockProject("dummy")

        return ProjectTypes.entries.map { projectType ->
            dynamicTest("dummy${projectType.suffix} should be $projectType") {
                // GIVEN
                val project = if (projectType == ProjectTypes.ROOT) {
                    rootProject
                } else {
                    mockProject("dummy${projectType.suffix}", rootProject)
                }

                // WHEN
                val actual = project.getType()

                // THEN
                assertThat(actual).isEqualTo(projectType)
            }
        }
    }

    @TestFactory
    fun test_isTypeOf(): List<DynamicTest> {
        val rootProject = mockProject("dummy")

        return ProjectTypes.entries.flatMap { projectType ->
            ProjectTypes.entries.associate { "dummy${it.suffix}" to (it == projectType) }
                .map { (projectName, expected) ->
                    dynamicTest("project $projectName is $projectType type should be $expected") {
                        // GIVEN
                        val project = if ((projectType == ProjectTypes.ROOT) && (projectName == "dummy")) {
                            rootProject
                        } else {
                            mockProject(projectName, rootProject)
                        }

                        if ((projectName == "dummy") && (projectType != ProjectTypes.ROOT)) {
                            // WHEN / THEN
                            assertThatThrownBy { project.isTypeOf(projectType) }
                                .isInstanceOf(NoSuchElementException::class.java)
                        } else {
                            // WHEN
                            val actual = project.isTypeOf(projectType)

                            // THEN
                            assertThat(actual).isEqualTo(expected)
                        }
                    }
                }
        }
    }

    companion object {
        private fun mockProject(name: String, rootProject: Project? = null): Project =
            mockk<Project> {
                every { this@mockk.name } returns name
                every { logger.info(any()) } answers { println(args[0]) }
                every { this@mockk.rootProject } answers { rootProject ?: this@mockk }
            }
    }

}
