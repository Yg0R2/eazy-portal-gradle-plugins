package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.eazyportal.gradle.internal.assertion.PomAssert.Companion.assertThatHasEazyPortalValues
import org.eazyportal.gradle.internal.project.ExampleProjectBuilder
import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.ARTIFACT_ID
import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.VERSION
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
    fun `exposes an api configuration`(@TempDir projectDir: File) {
        buildExampleProject(projectDir) {
            withBuildScript {
                """
                tasks.register("verifyApiConfiguration") {
                    val hasApi = configurations.findByName("api") != null
                    doLast { println("api configuration: ${'$'}hasApi") }
                }
                """.trimIndent()
            }
        }

        val output = runGradleTask(projectDir, "verifyApiConfiguration", withPluginClasspath = true)

        assertThat(output).contains("api configuration: true")
    }

    @Test
    fun `publishes a maven publication with a sources jar and a javadoc jar`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        runGradleTask(projectDir, "assemble", "-Pversion=$VERSION", withPluginClasspath = true)

        assertThat(File(projectDir, "build/libs/$ARTIFACT_ID-$VERSION-sources.jar")).exists()
        assertThat(File(projectDir, "build/libs/$ARTIFACT_ID-$VERSION-javadoc.jar")).exists()
    }

    @Test
    fun `applies the central non-bare POM to the maven publication`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        runGradleTask(projectDir, "generatePomFileForMavenPublication", "-Pversion=$VERSION", withPluginClasspath = true)

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/maven/pom-default.xml"))
    }

    /** A minimal Java library project with a `maven` publication `from(components["java"])`. */
    private fun buildExampleProject(
        projectDir: File,
        builder: ExampleProjectBuilder.() -> ExampleProjectBuilder = { this },
    ) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.java-library-convention")
            .withJavaSource()
            .apply { builder(this) }
            .build()
    }

}
