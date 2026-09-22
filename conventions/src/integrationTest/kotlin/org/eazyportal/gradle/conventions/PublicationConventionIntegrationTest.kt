package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.conventions.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.conventions.GradleUtils.runGradleTask
import org.eazyportal.gradle.conventions.assertion.PomAssert.Companion.assertThatHasEazyPortalValues
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.publication-convention` (design §4b.1, TOOLS-62 acceptance criteria):
 * central POM applied to every publication (a `maven` publication and a plugin-marker publication),
 * SNAPSHOT/release repository routing, and typed lazy GitHub Packages credentials.
 */
class PublicationConventionIntegrationTest {

    @Test
    fun `applies the central POM to a maven publication and a plugin-marker publication`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        runGradleTask(
            projectDir,
            "generatePomFileForMavenPublication",
            "generatePomFileForExampleEazyPortalPluginPluginMarkerMavenPublication",
            "-Pversion=$EXAMPLE_PROJECT_VERSION",
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
    fun `a SNAPSHOT version routes publish to publishToMavenLocal`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = runGradleTask(projectDir, "publish", "-Pversion=$EXAMPLE_PROJECT_VERSION-SNAPSHOT", "-m")

        assertThat(output).contains(":publishToMavenLocal").contains(":publish")
    }

    @Test
    fun `a release version wires the GitHubPackages repository`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            rootProject {
                script {
                    $$"""
                    tasks.register("printRepositories") {
                        val names = publishing.repositories.map { it.name }
                        doLast { println("repositories: $names") }
                    }
                    """.trimIndent()
                }
            }
        }

        val output = runGradleTask(projectDir, "printRepositories", "-Pversion=$EXAMPLE_PROJECT_VERSION")

        assertThat(output).contains("repositories: [GitHubPackages]")
    }

    @Test
    fun `publishing a release without credentials fails with the typed credentials error`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = runFailingGradleTask(
            projectDir,
            "publishMavenPublicationToGitHubPackagesRepository",
            "-Pversion=$EXAMPLE_PROJECT_VERSION",
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
        workingDir: File,
        configure: ExampleProjectBuilder.() -> Unit = { },
    ): File =
        exampleProject(workingDir) {
            rootProject {
                plugins("java-library", "java-gradle-plugin", "org.eazyportal.gradle.publication-convention")

                group = "org.eazyportal.example.plugin"
                description = "Example EazyPortal plugin"

                script {
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
                }
                script {
                    """
                    publishing {
                        publications {
                            create<MavenPublication>("maven") {
                                from(components["java"])
                            }
                        }
                    }
                    """.trimIndent()
                }

                javaSource("org/eazyportal/example/ExampleEazyPortalPlugin.java") {
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
                }
            }

            settings {
                rootProjectName = "example-eazyportal-plugin"
            }

            configure()
        }

}
