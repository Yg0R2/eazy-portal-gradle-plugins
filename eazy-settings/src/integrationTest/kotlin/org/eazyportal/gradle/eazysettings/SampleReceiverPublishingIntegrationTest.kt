package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.assertion.DiagnosticsAssert
import org.eazyportal.gradle.eazyproject.assertion.DiagnosticsAssert.EazyPortalCoreExpectation
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_LIBRARY_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.API
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.IMPLEMENTATION
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_RELEASE_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_SNAPSHOT_VERSION
import org.eazyportal.gradle.utils.repository.ExampleRepositoryBuilder.Companion.exampleRepository
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TOOLS-73 (design §7.4/§7.5, §9.5): a synthetic `sample-receiver`, scaffolded fresh per run by
 * [org.eazyportal.gradle.utils.project.ExampleProjectBuilder] — real §7.5 receiver bootstrap
 * (`eazy-settings` applied by bare id from the settings `plugins { }` block, exactly the real
 * README Quickstart path), over the full standard module set. Each module resolves to its correct
 * archetype + convention + sibling/eazyportal-core wiring (the TOOLS-66 matrix already unit-tested by
 * `ProjectConfigurerWiringTest` / `EazyProjectArchetypeMappingFunctionalTest`, here proven through one real,
 * wired-together receiver build) and publishes — as a release and as a SNAPSHOT version.
 *
 * Hermetic/offline specifically for the eazyportal-core supply chain, the actual subject of this technique:
 * eazyportal-core artifacts resolve from a stub repository built and published fresh into this @TempDir — the real
 * GitHub Packages repository is registered (design §6.5) but never hit (only stubbed, never-used
 * credentials are supplied — Gradle requires its typed [org.gradle.api.credentials.PasswordCredentials]
 * to be present even though `exclusiveContent` guarantees it is never queried) — and every published
 * artifact lands in an isolated `-Dmaven.repo.local` repository, never `~/.m2`.
 * `GRADLE_USER_HOME` is isolated per run too, so no ambient cache can mask a resolution gap.
 * (Kotlin-stdlib itself still resolves from `mavenCentral()` — a baseline every build in this project
 * already depends on, design decision noted on [EazySettingsToolchainIntegrationTest].)
 */
class SampleReceiverPublishingIntegrationTest {

    @Test
    fun `the sample receiver resolves every archetype's wiring and publishes a release version, fully offline`(
        @TempDir workingDir: File,
    ) {
        val repositoryDir = exampleRepository(workingDir, CORE_DEFAULT_VERSION) {
            eazyportalArtifacts()
        }

        val projectDir = buildExampleProject(workingDir, repositoryDir)

        val gradleHomeDir = File(workingDir, "gradle-home").also { it.mkdirs() }

        val output = gradleRunner(projectDir) {
            gradleUserHome = gradleHomeDir

            stubGitHubCredentials()

            stubSigningCredentials()
        }.runGradleTask(
            *PROJECT_EXPECTATIONS.map { ":${it.projectName}:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME" }.toTypedArray(),
            "publishToMavenLocal",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
            "-Pversion=$EXAMPLE_PROJECT_RELEASE_VERSION",
        )

        PROJECT_EXPECTATIONS.forEach { expectation ->
            expectation.assertDiagnostics(output)

            if (expectation.isPublishing) {
                File(
                    repositoryDir,
                    "org/eazyportal/example/${expectation.projectName}/$EXAMPLE_PROJECT_RELEASE_VERSION/${expectation.projectName}-$EXAMPLE_PROJECT_RELEASE_VERSION.jar"
                ).run {
                    assertThat(this).exists()
                }
            } else {
                assertThat(File(repositoryDir, "org/eazyportal/example/${expectation.projectName}"))
                    .doesNotExist()
            }
        }
    }

    @Test
    fun `the sample receiver resolves every archetype's wiring and publishes a SNAPSHOT version, fully offline`(
        @TempDir workingDir: File,
    ) {
        val repositoryDir = exampleRepository(workingDir, CORE_DEFAULT_VERSION) {
            eazyportalArtifacts()
        }

        val projectDir = buildExampleProject(workingDir, repositoryDir)

        val gradleHomeDir = File(workingDir, "gradle-home").also { it.mkdirs() }

        val output = gradleRunner(projectDir) {
            gradleUserHome = gradleHomeDir

            stubGitHubCredentials()
        }.runGradleTask(
            *PROJECT_EXPECTATIONS.map { ":${it.projectName}:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME" }.toTypedArray(),
            "publishToMavenLocal",
            "-Dmaven.repo.local=${repositoryDir.absolutePath}",
            "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION",
        )

        PROJECT_EXPECTATIONS.forEach { expectation ->
            expectation.assertDiagnostics(output)

            if (expectation.isPublishing) {
                File(
                    repositoryDir,
                    "org/eazyportal/example/${expectation.projectName}/$EXAMPLE_PROJECT_SNAPSHOT_VERSION/${expectation.projectName}-$EXAMPLE_PROJECT_SNAPSHOT_VERSION.jar"
                ).run {
                    assertThat(this).exists()
                }
            } else {
                assertThat(File(repositoryDir, "org/eazyportal/example/${expectation.projectName}"))
                    .doesNotExist()
            }
        }
    }

    private fun buildExampleProject(
        workingDir: File,
        repositoryDir: File,
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)

                script {
                    """
                    dependencyResolutionManagement {
                        repositories {
                            exclusiveContent {
                                forRepository {
                                    maven { url = uri("${repositoryDir.toURI()}") }
                                }

                                filter {
                                    includeGroup("$CORE_GROUP_ID")
                                }
                            }
                        }
                    }
                    """.trimIndent()
                }
            }

            PROJECT_EXPECTATIONS.forEach {
                subproject(it.projectName)
            }
        }

    /** One row of the TOOLS-66 wiring matrix (design §5.4), expressed as the exact `eazyDiagnostics` output it must produce. */
    private data class ProjectExpectation(
        val projectName: String,
        val convention: String = KOTLIN_LIBRARY_CONVENTION,
        val siblingProjects: List<Pair<String, DependencyConfiguration>> = emptyList(),
        val coreConfiguration: DependencyConfiguration = IMPLEMENTATION,
    ) {

        val isPublishing: Boolean =
            convention == KOTLIN_LIBRARY_CONVENTION

        fun assertDiagnostics(output: String): DiagnosticsAssert =
            DiagnosticsAssert.assertThat(output)
                .contains("> Task :$projectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")
                .hasDiagnostics(
                    projectName = projectName,
                    convention = convention,
                    siblingProjects = siblingProjects,
                    eazyPortalCore = EazyPortalCoreExpectation(configuration = coreConfiguration),
                )

    }

    private companion object {
        val PROJECT_EXPECTATIONS = listOf(
            ProjectExpectation(
                projectName = "common",
            ),
            ProjectExpectation(
                projectName = "api",
                siblingProjects = listOf("common" to API),
                coreConfiguration = API,
            ),
            ProjectExpectation(
                projectName = "persistence",
                siblingProjects = listOf("common" to IMPLEMENTATION),
            ),
            ProjectExpectation(
                projectName = "service",
                siblingProjects = listOf(
                    "common" to IMPLEMENTATION,
                    "api" to IMPLEMENTATION,
                    "persistence" to IMPLEMENTATION,
                ),
            ),
            ProjectExpectation(
                projectName = "client",
                siblingProjects = listOf(
                    "common" to API,
                    "api" to API,
                ),
                coreConfiguration = API,
            ),
            ProjectExpectation(
                projectName = "web",
                siblingProjects = listOf(
                    "common" to IMPLEMENTATION,
                    "api" to IMPLEMENTATION,
                    "service" to IMPLEMENTATION,
                ),
            ),
            ProjectExpectation(
                projectName = "application",
                convention = KOTLIN_PROJECT_CONVENTION,
                siblingProjects = listOf(
                    "common" to IMPLEMENTATION,
                    "api" to IMPLEMENTATION,
                    "persistence" to IMPLEMENTATION,
                    "service" to IMPLEMENTATION,
                    "web" to IMPLEMENTATION,
                ),
            ),
            ProjectExpectation(
                projectName = "new-module",
                convention = KOTLIN_PROJECT_CONVENTION,
            ),
        )
    }

}
