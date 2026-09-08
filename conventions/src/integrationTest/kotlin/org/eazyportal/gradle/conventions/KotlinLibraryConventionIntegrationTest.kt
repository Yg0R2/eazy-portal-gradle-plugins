package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.conventions.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.conventions.GradleUtils.runGradleTask
import org.eazyportal.gradle.conventions.assertion.PomAssert.Companion.assertThatHasEazyPortalValues
import org.eazyportal.gradle.conventions.project.ExampleProjectBuilder
import org.eazyportal.gradle.conventions.project.ExampleProjectFixtures.ARTIFACT_ID
import org.eazyportal.gradle.conventions.project.ExampleProjectFixtures.VERSION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.kotlin-library-convention` (design §4.4, TOOLS-63 acceptance criteria):
 * `explicitApi()` rejects a public declaration missing an explicit visibility/return type — and is *not* enforced
 * by `kotlin-project-convention` — the convention exposes an `api` configuration, and it produces a `maven`
 * publication with a sources jar (no Javadoc jar) plus the central (non-bare) POM from `publication-convention`.
 */
class KotlinLibraryConventionIntegrationTest {

    @Test
    fun `a public declaration missing an explicit return type fails under explicitApi`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-library-convention")
            .withMavenCentral()
            .withKotlinSource {
                KOTLIN_SOURCE_WITHOUT_EXPLICIT_TYPES
            }.build()

        val output = runFailingGradleTask(projectDir, "compileKotlin", withPluginClasspath = true)

        assertThat(output).contains("Return type must be specified in explicit API mode")
    }

    @Test
    fun `the same declaration compiles under kotlin-project-convention`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-project-convention")
            .withMavenCentral()
            .withKotlinSource {
                KOTLIN_SOURCE_WITHOUT_EXPLICIT_TYPES
            }.build()

        runGradleTask(projectDir, "compileKotlin", withPluginClasspath = true)
    }

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
    fun `publishes a maven publication with a sources jar and no javadoc jar`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        runGradleTask(projectDir, "assemble", "-Pversion=$VERSION", withPluginClasspath = true)

        assertThat(File(projectDir, "build/libs/$ARTIFACT_ID-$VERSION-sources.jar")).exists()
        assertThat(File(projectDir, "build/libs/$ARTIFACT_ID-$VERSION-javadoc.jar")).doesNotExist()
    }

    @Test
    fun `applies the central non-bare POM to the maven publication`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        runGradleTask(projectDir, "generatePomFileForMavenPublication", "-Pversion=$VERSION", withPluginClasspath = true)

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/maven/pom-default.xml"))
    }

    /** A minimal, `explicitApi`-compliant Kotlin library project with a `maven` publication `from(components["java"])`. */
    private fun buildExampleProject(
        projectDir: File,
        builder: ExampleProjectBuilder.() -> ExampleProjectBuilder = { this },
    ) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-library-convention")
            .withMavenCentral()
            .withKotlinSource {
                EXPLICIT_API_COMPLIANT_KOTLIN_SOURCE
            }.apply { builder(this) }
            .build()
    }

    companion object {
        /** A public top-level function relying on the default (implicit) visibility and inferred return type. */
        private val KOTLIN_SOURCE_WITHOUT_EXPLICIT_TYPES =
            """
            package org.eazyportal.example

            fun greetings() = "Hello World!"
            """.trimIndent()

        /** A public top-level function with an explicit visibility modifier and an explicit return type. */
        private val EXPLICIT_API_COMPLIANT_KOTLIN_SOURCE =
            """
            package org.eazyportal.example

            public fun greetings(): String = "Hello World!"
            """.trimIndent()
    }

}
