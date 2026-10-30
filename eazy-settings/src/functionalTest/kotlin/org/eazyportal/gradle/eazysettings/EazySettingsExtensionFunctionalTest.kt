package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.assertion.DiagnosticsAssert
import org.eazyportal.gradle.eazyproject.assertion.DiagnosticsAssert.EazyPortalCoreExpectation
import org.eazyportal.gradle.eazyproject.assertion.DiagnosticsAssert.Companion.assertThat
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_VERSION
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for the `eazySettings { eazyPortalCoreVersion / framework }` values (design §6.3/§6.4)
 * fed to every subproject's `eazyProject { }`: real Gradle builds driven through TestKit's plugin-under-test classpath.
 * The subprojects are named outside the standard archetype set (design §5.2), so they fall back to the `default` archetype and stay hermetic.
 */
class EazySettingsExtensionFunctionalTest {

    @Test
    fun `eazy-project's own default applies when the DSL override is absent`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras")

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).hasDiagnostics("extras", KOTLIN_PROJECT_CONVENTION)
    }

    @Test
    fun `the eazySettings framework is fed to every subproject`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                script {
                    generateEazySettingsString()
                }
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).hasCustomDiagnostics("extras")
    }

    @Test
    fun `the eazySettings override wins over eazy-project's own default`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                script {
                    generateEazySettingsString()
                }
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).hasCustomDiagnostics("extras")
    }

    @Test
    fun `CC double-run - override wins, the 2nd run reuses the CC entry, and zero CC problems occur`(@TempDir workingDir: File) {
        val gradleRunner = buildExampleProject(workingDir, "extras-a", "extras-b") {
            settings {
                script {
                    generateEazySettingsString()
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
            .hasCustomDiagnostics("extras-a")
            .hasCustomDiagnostics("extras-b")
            .doesNotContain("problem was found", "problems were found")

        val secondRun = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(secondRun)
            .contains("Reusing configuration cache.")
            .hasCustomDiagnostics("extras-a")
            .hasCustomDiagnostics("extras-b")
            .doesNotContain("problem was found", "problems were found")
    }

    private fun buildExampleProject(
        workingDir: File,
        vararg subprojectNames: String,
        builder: ExampleProjectBuilder.() -> Unit = { },
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)
            }

            subprojectNames.forEach {
                subproject(it)
            }

            builder()
        }

    private fun generateEazySettingsString(): String =
        """
        $EAZY_SETTINGS_EXTENSION_NAME {
            eazyPortalCoreVersion = "$CORE_VERSION"
            framework = ${Framework::class.java.name}.${Framework.SPRING_BOOT.name}
        }
        """.trimIndent()

    /** The block of a module that inherited the `eazySettings` version and framework from [generateEazySettingsString]. */
    private fun DiagnosticsAssert.hasCustomDiagnostics(projectName: String): DiagnosticsAssert =
        hasDiagnostics(
            projectName = projectName,
            convention = KOTLIN_PROJECT_CONVENTION,
            eazyPortalCore = EazyPortalCoreExpectation(
                version = CORE_VERSION,
                framework = Framework.SPRING_BOOT,
            ),
        )

}
