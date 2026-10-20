package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.conventions.assertion.PomAssert
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
 * TestKit coverage for `org.eazyportal.gradle.java-platform-convention`: applies `java-platform` (and not `java`),
 * allows direct dependencies next to constraints, publishes a `maven` publication from the `javaPlatform`
 * component with the central POM, and inherits the routing and signing behaviour of `publication-convention`.
 *
 * The routing and signing cases are deliberately thin: the mechanism itself is covered by
 * [PublicationConventionIntegrationTest]; here we only prove the platform convention wires it up.
 */
class JavaPlatformConventionIntegrationTest {

    @Test
    fun `allows direct dependencies next to constraints`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                """
                dependencies {
                    api("org.example:direct:1.0")
                }
                """.trimIndent()
            }
        }

        gradleRunner(projectDir)
            .runGradleTask("generatePomFileForMavenPublication", "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION")

        PomAssert.assertThat(pomOf(projectDir))
            .hasTextAt("dependencies/dependency/groupId", "org.example")
            .hasTextAt("dependencies/dependency/artifactId", "direct")
            .hasTextAt("dependencies/dependency/version", "1.0")
    }

    @Test
    fun `declares constraints in the dependencyManagement section`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                """
                dependencies {
                    constraints {
                        api("org.example:constrained:2.0")
                    }
                }
                """.trimIndent()
            }
        }

        gradleRunner(projectDir)
            .runGradleTask("generatePomFileForMavenPublication", "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION")

        PomAssert.assertThat(pomOf(projectDir))
            .hasTextAt("dependencyManagement/dependencies/dependency/groupId", "org.example")
            .hasTextAt("dependencyManagement/dependencies/dependency/artifactId", "constrained")
            .hasTextAt("dependencyManagement/dependencies/dependency/version", "2.0")
    }

    @Test
    fun `creates a single maven publication from the javaPlatform component`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                $$"""
                tasks.register("printPublications") {
                    val names = publishing.publications.names.toList()
                    doLast { println("publications: $names") }
                }
                """.trimIndent()
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask("printPublications")

        assertThat(output).contains("publications: [maven]")
    }

    @Test
    fun `publishes a pom-packaged POM with the central EazyPortal metadata`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        gradleRunner(projectDir)
            .runGradleTask("generatePomFileForMavenPublication", "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION")

        assertThatHasEazyPortalValues(pomOf(projectDir))
            .isPackagingEqualTo("pom")
    }

    @Test
    fun `does not apply the java plugin nor create compile, jar or test suite tasks`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                $$"""
                tasks.register("verifyNoJava") {
                    val hasJavaPlugin = plugins.hasPlugin("java")
                    val unexpected = tasks.names.filter {
                        it in setOf("compileJava", "jar", "sourcesJar", "javadocJar", "test", "functionalTest", "integrationTest")
                    }
                    doLast {
                        println("java plugin: $hasJavaPlugin")
                        println("unexpected tasks: $unexpected")
                    }
                }
                """.trimIndent()
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask("verifyNoJava")

        assertThat(output)
            .contains("java plugin: false")
            .contains("unexpected tasks: []")
    }

    @Test
    fun `fails when combined with the java-library plugin`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            plugins("java-library")
        }

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("help")

        assertThat(output).contains("cannot be applied together")
    }

    @Test
    fun `a SNAPSHOT version routes publish to publishToMavenLocal`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runGradleTask("publish", "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION", "-m")

        assertThat(output).contains(":publishToMavenLocal").contains(":publish")
    }

    @Test
    fun `a release version wires the GitHubPackages repository`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                $$"""
                tasks.register("printRepositories") {
                    val names = publishing.repositories.map { it.name }
                    doLast { println("repositories: $names") }
                }
                """.trimIndent()
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask("printRepositories", "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION")

        assertThat(output).contains("repositories: [GitHubPackages]")
    }

    @Test
    fun `a release version, publish without a signing key fails and publishes nothing`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)
        val repositoryDir = File(workingDir, "repo")

        val output = gradleRunner(projectDir)
            .runFailingGradleTask(
                "publishToMavenLocal",
                "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION",
                "-Dmaven.repo.local=${repositoryDir.absolutePath}",
            )

        assertThat(output)
            .contains("Execution failed for task ':signMavenPublication'")
            .contains("No configured signatory")
        assertThat(publishedDir(repositoryDir, EXAMPLE_PROJECT_RELEASE_VERSION)).doesNotExist()
    }

    @Test
    fun `a release version, publish with a test signing key signs every published file`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)
        val repositoryDir = File(workingDir, "repo")

        gradleRunner(projectDir) {
            stubSigningCredentials()
        }.runGradleTask(
            "publishToMavenLocal",
            "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
        )

        val artifactDir = publishedDir(repositoryDir, EXAMPLE_PROJECT_RELEASE_VERSION)
        val publishedFiles = artifactDir
            .listFiles { file -> !file.name.endsWith(".asc") }
            .orEmpty()

        assertThat(publishedFiles).isNotEmpty()
        publishedFiles.forEach { file ->
            assertThat(File(artifactDir, "${file.name}.asc")).exists()
        }
    }

    @Test
    fun `a SNAPSHOT version, publish produces no signatures even with a signing key present`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)
        val repositoryDir = File(workingDir, "repo")

        gradleRunner(projectDir) {
            stubSigningCredentials()
        }.runGradleTask(
            "publishToMavenLocal",
            "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
        )

        assertThat(publishedDir(repositoryDir, EXAMPLE_PROJECT_SNAPSHOT_VERSION)).isDirectory()
        Files.walk(repositoryDir.toPath()).use { paths ->
            assertThat(paths.filter { it.name.endsWith(".asc") }.toList()).isEmpty()
        }
    }

    private fun pomOf(projectDir: File): File =
        File(projectDir, "build/publications/maven/pom-default.xml")

    private fun publishedDir(repositoryDir: File, version: String): File =
        File(repositoryDir, "org/eazyportal/example/$EXAMPLE_ROOT_PROJECT_NAME/$version")

    /** A minimal Java platform project (no sources) with a `maven` publication `from(components["javaPlatform"])`. */
    private fun buildExampleProject(
        workingDir: File,
        configureRoot: ExampleProjectBuilder.RootProjectBuilder.() -> Unit = {},
    ): File =
        exampleProject(workingDir) {
            rootProject {
                plugins("org.eazyportal.gradle.java-platform-convention")

                configureRoot()
            }
        }

}
