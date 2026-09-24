package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.conventions.assertion.PomAssert.Companion.assertThatHasEazyPortalValues
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_NAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.java-library-convention` (design §4.4, TOOLS-63 acceptance criteria):
 * exposes an `api` configuration, produces a `maven` publication with a sources jar and a Javadoc jar, and
 * reuses the central (non-bare) POM from `publication-convention`.
 */
class JavaLibraryConventionIntegrationTest {

    @Test
    fun `exposes an api configuration`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                $$"""
                tasks.register("verifyApiConfiguration") {
                    val hasApi = configurations.findByName("api") != null
                    doLast { println("api configuration: $hasApi") }
                }
                """.trimIndent()
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask("verifyApiConfiguration")

        assertThat(output).contains("api configuration: true")
    }

    @Test
    fun `publishes a maven publication with a sources jar and a javadoc jar`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        gradleRunner(projectDir)
            .runGradleTask("assemble", "-Pversion=$EXAMPLE_PROJECT_VERSION")

        assertThat(File(projectDir, "build/libs/$EXAMPLE_ROOT_PROJECT_NAME-$EXAMPLE_PROJECT_VERSION-sources.jar")).exists()
        assertThat(File(projectDir, "build/libs/$EXAMPLE_ROOT_PROJECT_NAME-$EXAMPLE_PROJECT_VERSION-javadoc.jar")).exists()
    }

    @Test
    fun `applies the central non-bare POM to the maven publication`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        gradleRunner(projectDir)
            .runGradleTask("generatePomFileForMavenPublication", "-Pversion=$EXAMPLE_PROJECT_VERSION")

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/maven/pom-default.xml"))
    }

    /** A minimal Java library project with a `maven` publication `from(components["java"])`. */
    private fun buildExampleProject(
        workingDir: File,
        configureRoot: ExampleProjectBuilder.RootProjectBuilder.() -> Unit = {},
    ): File =
        exampleProject(workingDir) {
            rootProject {
                plugins("org.eazyportal.gradle.java-library-convention")

                javaSource()

                configureRoot()
            }
        }

}
