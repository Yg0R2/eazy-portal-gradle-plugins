package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryBuilder.Companion.exampleRepository
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_COMMON_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for `org.eazyportal.gradle.eazy-settings` (design §6.3/§6.5, TOOLS-68 acceptance criteria):
 * real Gradle builds, driven through TestKit's plugin-under-test classpath,
 * applying the plugin by bare id in a synthetic receiver's `settings.gradle.kts` `plugins { }` block —
 * the normal path a receiver repo uses.
 *
 * Subprojects are named outside the standard archetype set (design §5.2) on purpose:
 * they fall back to `ProjectType.DEFAULT` / `kotlin-project-convention`,
 * which needs no `eazyportal-core` repository to resolve —
 * keeping these tests hermetic while still exercising the auto-apply + version-resolution behavior under test here.
 */
class EazySettingsPluginFunctionalTest {

    @Test
    fun `every subproject gets eazy-project auto-applied, the root does not`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras-a", "extras-b")
        val gradleRunner = gradleRunner(projectDir)

        val output = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME"
        )

        assertThat(output)
            .contains("eazy-project diagnostics — :extras-a")
            .contains("eazy-project diagnostics — :extras-b")

        val rootOutput = gradleRunner.runFailingGradleTask(":$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(rootOutput).contains("not found in root project")
    }

    @Test
    fun `eazy-project's own default applies when the DSL override is absent`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras")

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).contains("eazyPortalCoreVersion  : $EXAMPLE_CORE_DEFAULT_VERSION")
    }

    @Test
    fun `the eazySettings override wins over eazy-project's own default`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                script {
                    generateEazySettingsString("9.9.9")
                }
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).contains("eazyPortalCoreVersion  : 9.9.9")
    }

    @Test
    fun `CC double-run - override wins, the 2nd run reuses the CC entry, and zero CC problems occur`(@TempDir workingDir: File) {
        val gradleRunner = buildExampleProject(workingDir, "extras-a", "extras-b") {
            settings {
                script {
                    generateEazySettingsString("9.9.9")
                }
            }
        }.let(::gradleRunner)

        val firstRun = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(firstRun)
            .contains("Configuration cache entry stored.")
            .contains("eazyPortalCoreVersion  : 9.9.9")
            .doesNotContain("problem was found", "problems were found")

        val secondRun = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(secondRun)
            .contains("Reusing configuration cache.")
            .contains("eazyPortalCoreVersion  : 9.9.9")
            .doesNotContain("problem was found", "problems were found")
    }

    @Test
    fun `a module declaring its own repositories fails the build (FAIL_ON_PROJECT_REPOS)`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            subproject("extras") {
                useMavenCentral()
            }
        }

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("help")

        assertThat(output).contains("Build was configured to prefer settings repositories over project repositories")
    }

    @Test
    fun `the hermetic stub resolves eazyportal-core with no credentials — the real GitHub repo is registered but never hit`(
        @TempDir workingDir: File,
    ) {
        val exampleRepositoryDir = exampleRepository(workingDir, EXAMPLE_CORE_DEFAULT_VERSION) {
            eazyportalArtifacts()
        }

        val projectDir = buildExampleProject(workingDir, "common") {
            settings {
                script {
                    """
                    dependencyResolutionManagement {
                        repositories {
                            exclusiveContent {
                                forRepository {
                                    maven { url = uri("${exampleRepositoryDir.toURI()}") }
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
        }

        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val output = gradleRunner(projectDir) {
            stubEnvironment(gradleHomeDir)
        }.runGradleTask(":common:dependencies", "--configuration", "compileClasspath")

        assertThat(output).contains("$CORE_GROUP_ID:$CORE_COMMON_ARTIFACT_ID:$EXAMPLE_CORE_DEFAULT_VERSION")
    }

    @Test
    fun `forcing resolution from the real GitHub repo without credentials fails with a clear error, never silent anonymous`(
        @TempDir workingDir: File,
    ) {
        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val projectDir = buildExampleProject(workingDir, "common")

        val output = gradleRunner(projectDir) {
            hermeticEnvironment(gradleHomeDir)
        }.runFailingGradleTask(":common:dependencies", "--configuration", "compileClasspath")

        assertThat(output)
            .contains("The following Gradle properties are missing for 'GitHubPackages' credentials")
            .doesNotContain("BUILD SUCCESSFUL")
    }

    /**
     * A synthetic receiver: an empty root applying `org.eazyportal.gradle.eazy-settings` via the settings `plugins { }`
     * block (the real receiver bootstrap, README Quickstart) + one subproject per [subprojectNames].
     */
    private fun buildExampleProject(
        workingDir: File,
        vararg subprojectNames: String,
        builder: ExampleProjectBuilder.() -> Unit =  { },
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins("org.eazyportal.gradle.eazy-settings")
            }

            subprojectNames.forEach {
                subproject(it)
            }

            builder()
        }

    private fun generateEazySettingsString(eazyPortalCoreVersionOverride: String): String =
        """
        $EAZY_SETTINGS_EXTENSION_NAME {
            eazyPortalCoreVersion = "$eazyPortalCoreVersionOverride"
        }
        """.trimIndent()

}
