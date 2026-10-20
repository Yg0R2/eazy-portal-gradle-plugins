package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for the `eazySettings { applyEazyPortalCoreDependencies = false }` escape hatch:
 * every eazyportal-core dependency (BOM and `eazyportal-core-test` included) is skipped, while the convention and the sibling wiring stay.
 * The flag is only final after the module's own build script ran, so a `eazyProject { }` override is covered too.
 */
class EazySettingsEazyPortalCoreToggleFunctionalTest {

    @Test
    fun `applyEazyPortalCoreDependencies=false skips eazyportal-core but keeps the sibling wiring`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "api", "client")

        val output = gradleRunner(projectDir)
            .runGradleTask(":client:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output)
            .contains("  siblings               : [:api (api)]")
            .contains("  eazyportal-core        : N/A   (disabled via DSL)")
            .contains("  applyEazyPortalCoreDependencies : false")
    }

    @Test
    fun `applyEazyPortalCoreDependencies=false resolves no eazyportal-core artifact`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "common")

        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        // The GitHubPackages repo is still declared, so any resolution requires its credentials — the toggle only drops the dependencies.
        val output = gradleRunner(projectDir) {
            gradleUserHome = gradleHomeDir

            stubGitHubCredentials()
        }.runGradleTask(":common:dependencies", "--configuration", "testCompileClasspath")

        assertThat(output).doesNotContain(CORE_GROUP_ID)
    }

    @Test
    fun `a subproject's own eazyProject override re-enables eazyportal-core for that module only`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras-a", "extras-b") {
            subproject("extras-a") {
                script {
                    """
                    $EAZY_PROJECT_EXTENSION_NAME {
                        applyEazyPortalCoreDependencies = true
                    }
                    """.trimIndent()
                }
            }
        }

        val gradleRunner = gradleRunner(projectDir)

        assertThat(gradleRunner.runGradleTask(":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME"))
            .contains("(versions via the eazyportal-core BOM)")
            .contains("  applyEazyPortalCoreDependencies : true")
        assertThat(gradleRunner.runGradleTask(":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME"))
            .contains("  eazyportal-core        : N/A   (disabled via DSL)")
            .contains("  applyEazyPortalCoreDependencies : false")
    }

    @Test
    fun `CC double-run - the toggle survives a configuration cache reuse with zero CC problems`(@TempDir workingDir: File) {
        val gradleRunner = buildExampleProject(workingDir, "extras").let(::gradleRunner)

        repeat(2) { run ->
            val output = gradleRunner.runGradleTask(
                ":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
                "--configuration-cache",
                "--configuration-cache-problems=fail",
            )

            assertThat(output)
                .contains(if (run == 0) "Configuration cache entry stored." else "Reusing configuration cache.")
                .contains("  applyEazyPortalCoreDependencies : false")
                .doesNotContain("problem was found", "problems were found")
        }
    }

    /** A receiver whose `eazySettings { }` disables eazyportal-core for every subproject. */
    private fun buildExampleProject(
        workingDir: File,
        vararg subprojectNames: String,
        builder: ExampleProjectBuilder.() -> Unit = { },
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)

                script {
                    """
                    $EAZY_SETTINGS_EXTENSION_NAME {
                        applyEazyPortalCoreDependencies = false
                    }
                    """.trimIndent()
                }
            }

            subprojectNames.forEach {
                subproject(it)
            }

            builder()
        }

}
