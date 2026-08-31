package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.eazyportal.gradle.internal.assertion.PomAssert.Companion.assertThatHasEazyPortalValues
import org.eazyportal.gradle.internal.project.ExampleProjectBuilder
import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.VERSION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.publication-convention` (design §4b.1, TOOLS-62 acceptance criteria):
 * central POM applied to every publication (a `maven` publication and a plugin-marker publication),
 * SNAPSHOT/release repository routing, and typed lazy GitHub Packages credentials.
 */
class PublicationConventionTest {

    @Test
    fun `applies the central POM to a maven publication and a plugin-marker publication`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        runGradleTask(
            projectDir,
            "generatePomFileForMavenPublication",
            "generatePomFileForExampleEazyPortalPluginPluginMarkerMavenPublication",
            "-Pversion=$VERSION",
            withPluginClasspath = true,
        )

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/maven/pom-default.xml")) {
            groupId = "org.eazyportal.example.plugin"
            artifactId = "example-eazyportal-plugin"
            description = "Example EazyPortal plugin"
            name = "example-eazyportal-plugin"
        }

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/exampleEazyPortalPluginPluginMarkerMaven/pom-default.xml")) {
            groupId = "org.eazyportal.example.plugin"
            artifactId = "org.eazyportal.example.plugin.gradle.plugin"
            description = "Example EazyPortal plugin"
            name = "example-eazyportal-plugin"
        }
    }

    @Test
    fun `a SNAPSHOT version routes publish to publishToMavenLocal`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runGradleTask(projectDir, "publish", "-Pversion=$VERSION-SNAPSHOT", "-m", withPluginClasspath = true)

        assertThat(output).contains(":publishToMavenLocal").contains(":publish")
    }

    @Test
    fun `a release version wires the GitHubPackages repository`(@TempDir projectDir: File) {
        buildExampleProject(projectDir) {
            withBuildScript {
                """
                tasks.register("printRepositories") {
                    val names = publishing.repositories.map { it.name }
                    doLast { println("repositories: ${'$'}names") }
                }
                """.trimIndent()
            }
        }

        val output = runGradleTask(projectDir, "printRepositories", "-Pversion=$VERSION", withPluginClasspath = true)

        assertThat(output).contains("repositories: [GitHubPackages]")
    }

    @Test
    fun `publishing a release without credentials fails with the typed credentials error`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runFailingGradleTask(
            projectDir,
            "publishMavenPublicationToGitHubPackagesRepository",
            "-Pversion=$VERSION",
            withPluginClasspath = true,
        )

        assertThat(output)
            .contains("The following Gradle properties are missing for 'GitHubPackages' credentials:")
            .contains("- GitHubPackagesUsername")
            .contains("- GitHubPackagesPassword")
    }

    /**
     * A minimal java-gradle-plugin project with a `maven` publication,
     * exercising both the `maven` publication and the auto-generated `exampleEazyPortalPluginPluginMarkerMaven` plugin-marker publication.
     */
    private fun buildExampleProject(
        projectDir: File,
        builder: ExampleProjectBuilder.() -> ExampleProjectBuilder = { this },
    ) {
        ExampleProjectBuilder(projectDir)
            .withGroup("org.eazyportal.example.plugin")
            .withRootProjectName("example-eazyportal-plugin")
            .withDescription("Example EazyPortal plugin")
            .withBuildPlugins("java-library", "java-gradle-plugin", "org.eazyportal.gradle.publication-convention")
            .withBuildScript {
                """
                gradlePlugin {
                    plugins {
                        create("exampleEazyPortalPlugin") {
                            id = "org.eazyportal.example.plugin"
                            implementationClass = "org.eazyportal.example.ExampleEazyPortalPlugin"
                        }
                    }
                }
                """.trimIndent()
            }.withBuildScript {
                """
                publishing {
                    publications {
                        create<MavenPublication>("maven") {
                            from(components["java"])
                        }
                    }
                }
                """.trimIndent()
            }.withJavaSource("com/eazyportal/example/ExampleEazyPortalPlugin.java") {
                """
                package org.eazyportal.example;

                import org.gradle.api.Plugin;
                import org.gradle.api.Project;

                public class ExampleEazyPortalPlugin implements Plugin<Project> {
                    @Override
                    public void apply(Project project) {
                    }
                }
                """.trimIndent()
            }.apply {
                builder(this)
            }.build()
    }

}
