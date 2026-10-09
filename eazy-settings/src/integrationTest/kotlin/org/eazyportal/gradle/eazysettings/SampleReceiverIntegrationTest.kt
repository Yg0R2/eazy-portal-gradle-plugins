package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_LIBRARY_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.Companion.TEST_CONFIGURATIONS
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_RELEASE_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_SNAPSHOT_VERSION
import org.eazyportal.gradle.utils.repository.ExampleRepositoryBuilder.Companion.exampleRepository
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.createCoreArtifactId
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
 * `ProjectConfigurerWiringTest` / `EazyProjectPluginFunctionalTest`, here proven through one real,
 * wired-together receiver build) and publishes.
 *
 * Hermetic/offline specifically for the eazyportal-core supply chain, the actual subject of this technique:
 * eazyportal-core-* resolves from a stub repository built and published fresh into this @TempDir — the real
 * GitHub Packages repository is registered (design §6.5) but never hit (only stubbed, never-used
 * credentials are supplied — Gradle requires its typed [org.gradle.api.credentials.PasswordCredentials]
 * to be present even though `exclusiveContent` guarantees it is never queried) — and every published
 * artifact lands in an isolated `-Dmaven.repo.local` repository, never `~/.m2`.
 * `GRADLE_USER_HOME` is isolated per run too, so no ambient cache can mask a resolution gap.
 * (Kotlin-stdlib itself still resolves from `mavenCentral()` — a baseline every build in this project
 * already depends on, design decision noted on [EazySettingsPluginIntegrationTest].)
 */
class SampleReceiverIntegrationTest {

    @Test
    fun `the sample receiver resolves every archetype's wiring and publishes a release version, fully offline`(
        @TempDir workingDir: File,
    ) {
        val repositoryDir = exampleRepository(workingDir, EXAMPLE_CORE_DEFAULT_VERSION) {
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
            assertThat(output).containsSubsequence(*expectation.expectedDiagnosticsLines())

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
        val repositoryDir = exampleRepository(workingDir, EXAMPLE_CORE_DEFAULT_VERSION) {
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
            assertThat(output).containsSubsequence(*expectation.expectedDiagnosticsLines())

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

    @Test
    fun `eazyportal-core-test resolves versionless from the BOM on every test tier and on the test fixtures`(
        @TempDir workingDir: File,
    ) {
        val repositoryDir = exampleRepository(workingDir, EXAMPLE_CORE_DEFAULT_VERSION) {
            eazyportalArtifacts()
        }

        val projectDir = buildExampleProject(workingDir, repositoryDir)

        val gradleHomeDir = File(workingDir, "gradle-home").also { it.mkdirs() }

        listOf(
            "testCompileClasspath",
            "functionalTestCompileClasspath",
            "integrationTestCompileClasspath",
            "testFixturesCompileClasspath",
        ).forEach { classpath ->
            val output = gradleRunner(projectDir) {
                gradleUserHome = gradleHomeDir

                stubGitHubCredentials()
            }.runGradleTask(
                ":common:dependencyInsight",
                "--configuration",
                classpath,
                "--dependency",
                createCoreArtifactId("test"),
            )

            assertThat(output)
                .describedAs("eazyportal-core-test on $classpath")
                .contains("$CORE_GROUP_ID:${createCoreArtifactId("test")}:$EXAMPLE_CORE_DEFAULT_VERSION")
                .doesNotContain("FAILED")
        }
    }

    private fun buildExampleProject(
        workingDir: File,
        repositoryDir: File,
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins("org.eazyportal.gradle.eazy-settings")

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

            PROJECT_EXPECTATIONS.asSequence()
                .map { it.projectName }
                .forEach { projectName ->
                    subproject(projectName)
                }
        }

    /** One row of the TOOLS-66 wiring matrix (design §5.4), expressed as the exact `eazyDiagnostics` output it must produce. */
    private data class ProjectExpectation(
        val projectName: String,
        val convention: String = KOTLIN_LIBRARY_CONVENTION,
        val siblingProjects: List<Pair<String, String>> = emptyList(),
        val coreProjectType: ProjectType = ProjectType.DEFAULT,
        val coreConfiguration: String = "implementation",
    ) {

        val isPublishing: Boolean =
            convention == KOTLIN_LIBRARY_CONVENTION

        fun expectedDiagnosticsLines(): Array<String> {
            val siblingsText = siblingProjects
                .joinToString(", ", "[", "]") { (sibling, configuration) ->
                    ":$sibling ($configuration)"
                }

            val coreText = buildList {
                if (isPublishing) {
                    add("${createCoreArtifactId(coreProjectType.toString())} ($coreConfiguration)")
                }

                TEST_CONFIGURATIONS.forEach {
                    add("${createCoreArtifactId("test")} ($it)")
                }
            }.joinToString(", ", "[", "]")

            return arrayOf(
                "> Task :$projectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
                "eazy-project diagnostics — :$projectName",
                "  archetype              : $coreProjectType",
                "  convention             : [$convention]",
                "  siblings               : $siblingsText",
                "  eazyportal-core        : $coreText   (versions via the eazyportal-core BOM)",
                "  eazyPortalCoreVersion  : $EXAMPLE_CORE_DEFAULT_VERSION   (source: default (DefaultVersions))",
            )
        }

    }

    companion object {
        private val PROJECT_EXPECTATIONS = listOf(
            ProjectExpectation(
                projectName = "common",
                coreProjectType = ProjectType.COMMON,
            ),
            ProjectExpectation(
                projectName = "api",
                siblingProjects = listOf("common" to "api"),
                coreProjectType = ProjectType.API,
                coreConfiguration = "api",
            ),
            ProjectExpectation(
                projectName = "persistence",
                siblingProjects = listOf("common" to "implementation"),
                coreProjectType = ProjectType.PERSISTENCE,
            ),
            ProjectExpectation(
                projectName = "service",
                siblingProjects = listOf(
                    "common" to "implementation",
                    "api" to "implementation",
                    "persistence" to "implementation",
                ),
                coreProjectType = ProjectType.SERVICE,
            ),
            ProjectExpectation(
                projectName = "client",
                siblingProjects = listOf(
                    "common" to "api",
                    "api" to "api",
                ),
                coreProjectType = ProjectType.CLIENT,
                coreConfiguration = "api",
            ),
            ProjectExpectation(
                projectName = "web",
                siblingProjects = listOf(
                    "common" to "implementation",
                    "api" to "implementation",
                    "service" to "implementation",
                ),
                coreProjectType = ProjectType.WEB,
            ),
            ProjectExpectation(
                projectName = "application",
                convention = KOTLIN_PROJECT_CONVENTION,
                siblingProjects = listOf(
                    "common" to "implementation",
                    "api" to "implementation",
                    "persistence" to "implementation",
                    "service" to "implementation",
                    "client" to "implementation",
                    "web" to "implementation",
                ),
                coreProjectType = ProjectType.APPLICATION,
            ),
            ProjectExpectation(
                projectName = "new-module",
                convention = KOTLIN_PROJECT_CONVENTION,
            ),
        )
    }

}
