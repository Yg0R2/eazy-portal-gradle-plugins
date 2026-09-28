package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.conventions.assertion.PomAssert.Companion.assertThatHasEazyPortalValues
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_RELEASE_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_SNAPSHOT_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_NAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import kotlin.io.path.name

/**
 * TestKit coverage for `org.eazyportal.gradle.publication-convention` (design §4b.1, TOOLS-62 acceptance criteria):
 * central POM applied to every publication (a `maven` publication and a plugin-marker publication),
 * SNAPSHOT/release repository routing, and typed lazy GitHub Packages credentials.
 *
 * Also covers release-gated GPG signing end-to-end (design §9.5, TOOLS-72), using a throwaway test key ([TEST_SIGNING_KEY]/[TEST_SIGNING_PASSWORD]).
 */
class PublicationConventionIntegrationTest {

    @Test
    fun `applies the central POM to a maven publication`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        gradleRunner(projectDir)
            .runGradleTask(
                "generatePomFileForMavenPublication",
                "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION",
            )

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/maven/pom-default.xml"))
    }

    @Test
    fun `applies the central POM to a maven publication and a plugin-marker publication`(@TempDir workingDir: File) {
        val projectDir = buildExamplePluginProject(workingDir)

        gradleRunner(projectDir)
            .runGradleTask(
                "generatePomFileForPluginMavenPublication",
                "generatePomFileForExampleEazyPortalPluginPluginMarkerMavenPublication",
                "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION",
            )

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/pluginMaven/pom-default.xml")) {
            groupId = EXAMPLE_PLUGIN_GROUP_ID
            artifactId = EXAMPLE_PLUGIN_PROJECT_NAME
            description = EXAMPLE_PLUGIN_PROJECT_DESCRIPTION
            name = EXAMPLE_PLUGIN_PROJECT_NAME
        }

        assertThatHasEazyPortalValues(File(projectDir, "build/publications/exampleEazyPortalPluginPluginMarkerMaven/pom-default.xml")) {
            groupId = EXAMPLE_PLUGIN_GROUP_ID
            artifactId = EXAMPLE_PLUGIN_ARTIFACT_ID
            description = EXAMPLE_PLUGIN_PROJECT_DESCRIPTION
            name = EXAMPLE_PLUGIN_PROJECT_NAME
        }
    }

    @Test
    fun `a SNAPSHOT version routes publish to publishToMavenLocal`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runGradleTask("publish", "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION-SNAPSHOT", "-m")

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

        val output = gradleRunner(projectDir)
            .runGradleTask("printRepositories", "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION")

        assertThat(output).contains("repositories: [GitHubPackages]")
    }

    @Test
    fun `publishing a release without credentials fails with the typed credentials error`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runFailingGradleTask(
                "publishMavenPublicationToGitHubPackagesRepository",
                "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION",
            )

        assertThat(output)
            .contains("The following Gradle properties are missing for 'GitHubPackages' credentials:")
            .contains("- GitHubPackagesUsername")
            .contains("- GitHubPackagesPassword")
    }

    @Test
    fun `a gradle library with release version, publish with a test signing key signs every artifact and POM for the maven publications`(
        @TempDir workingDir: File,
    ) {
        val projectDir = buildExampleProject(workingDir)
        val repositoryDir = File(workingDir, "repo")

        gradleRunner(projectDir) {
            stubSigningCredentials()
        }.runGradleTask(
            "publishToMavenLocal",
            "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
        )

        assertThatEveryPublishedFileIsSigned(
            File(repositoryDir, "org/eazyportal/example/$EXAMPLE_ROOT_PROJECT_NAME/$EXAMPLE_PROJECT_RELEASE_VERSION"),
        )
    }

    @Test
    fun `a gradle plugin with release version, publish with a test signing key signs every artifact and POM for the maven and plugin-marker publications`(
        @TempDir workingDir: File,
    ) {
        val projectDir = buildExamplePluginProject(workingDir)
        val repositoryDir = File(workingDir, "repo")

        gradleRunner(projectDir) {
            stubSigningCredentials()
        }.runGradleTask(
            "publishToMavenLocal",
            "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
        )

        assertThatEveryPublishedFileIsSigned(
            File(repositoryDir, "org/eazyportal/example/plugin/$EXAMPLE_PLUGIN_PROJECT_NAME/$EXAMPLE_PROJECT_RELEASE_VERSION"),
        )
        assertThatEveryPublishedFileIsSigned(
            File(repositoryDir, "org/eazyportal/example/plugin/$EXAMPLE_PLUGIN_ARTIFACT_ID/$EXAMPLE_PROJECT_RELEASE_VERSION"),
        )
    }

    @Test
    fun `a gradle library with SNAPSHOT version, publish produces no signatures even with a signing key present`(
        @TempDir workingDir: File,
    ) {
        val projectDir = buildExampleProject(workingDir)
        val repositoryDir = File(workingDir, "repo")

        gradleRunner(projectDir) {
            stubSigningCredentials()
        }.runGradleTask(
            "publishToMavenLocal",
            "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION-SNAPSHOT",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
        )

        assertThatNeitherPublishedFileIsSigned(repositoryDir)
    }

    @Test
    fun `a gradle plugin with SNAPSHOT version, publish produces no signatures even with a signing key present`(
        @TempDir workingDir: File,
    ) {
        val projectDir = buildExampleProject(workingDir)
        val repositoryDir = File(workingDir, "repo")

        gradleRunner(projectDir) {
            stubSigningCredentials()
        }.runGradleTask(
            "publishToMavenLocal",
            "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION-SNAPSHOT",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
        )

        assertThatNeitherPublishedFileIsSigned(repositoryDir)
    }

    /** Every non-`.asc` file published for a coordinate (jar/pom/module) has a sibling `.asc` signature. */
    private fun assertThatEveryPublishedFileIsSigned(artifactDir: File) {
        val publishedFiles = artifactDir
            .listFiles { !it.name.endsWith(".asc") }
            .orEmpty()

        assertThat(publishedFiles).isNotEmpty()
        publishedFiles.forEach { file ->
            assertThat(File(artifactDir, "${file.name}.asc")).exists()
        }
    }

    /** Every non-`.asc` file published for a coordinate (jar/pom/module) has a sibling `.asc` signature. */
    private fun assertThatNeitherPublishedFileIsSigned(repositoryDir: File) {
        val signatures = Files.walk(repositoryDir.toPath())
            .filter { it.name.endsWith(".asc") }

        assertThat(signatures).isEmpty()
    }

    /**
     * A minimal java project.
     */
    private fun buildExampleProject(
        workingDir: File,
        configure: ExampleProjectBuilder.() -> Unit = { },
    ): File =
        exampleProject(workingDir) {
            rootProject {
                plugins("java-library", "org.eazyportal.gradle.publication-convention")

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

                javaSource()
            }

            configure()
        }

    /**
     * A minimal java-gradle-plugin project.
     */
    private fun buildExamplePluginProject(workingDir: File): File =
        exampleProject(workingDir) {
            rootProject {
                plugins("java-gradle-plugin", "org.eazyportal.gradle.publication-convention")

                group = EXAMPLE_PLUGIN_GROUP_ID
                description = EXAMPLE_PLUGIN_PROJECT_DESCRIPTION

                script {
                    """
                    gradlePlugin {
                        plugins {
                            create("exampleEazyPortalPlugin") {
                                id = "$EXAMPLE_PLUGIN_GROUP_ID"
                                implementationClass = "org.eazyportal.example.plugin.ExampleEazyPortalPlugin"
                            }
                        }
                    }
                    """.trimIndent()
                }

                javaSource("org/eazyportal/example/plugin/ExampleEazyPortalPlugin.java") {
                    """
                    package org.eazyportal.example.plugin;

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
                rootProjectName = EXAMPLE_PLUGIN_PROJECT_NAME
            }
        }

    companion object {
        private const val EXAMPLE_PLUGIN_PROJECT_NAME = "eazyportal-example-plugin"
        private const val EXAMPLE_PLUGIN_PROJECT_DESCRIPTION = "Example EazyPortal plugin"
        private const val EXAMPLE_PLUGIN_GROUP_ID = "org.eazyportal.example.plugin"
        private const val EXAMPLE_PLUGIN_ARTIFACT_ID = "$EXAMPLE_PLUGIN_GROUP_ID.gradle.plugin"
    }

}
