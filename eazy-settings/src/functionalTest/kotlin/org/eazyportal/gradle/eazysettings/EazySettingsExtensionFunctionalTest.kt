package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_VERSION
import org.assertj.core.api.Assertions.assertThat
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

        assertThat(output)
            .contains("eazyPortalCoreVersion  : $CORE_DEFAULT_VERSION   (source: default (DefaultVersions))")
            .contains("framework              : ${Framework.SPRING_BOOT}")
    }

    @Test
    fun `the eazySettings framework is fed to every subproject`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                generateEazySettingsString()
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).contains("framework              : ${Framework.SPRING_BOOT}")
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

        assertThat(output).contains("eazyPortalCoreVersion  : $CORE_VERSION   (source: DSL override)")
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
            .contains("eazyPortalCoreVersion  : $CORE_VERSION   (source: DSL override)")
            .doesNotContain("problem was found", "problems were found")

        val secondRun = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(secondRun)
            .contains("Reusing configuration cache.")
            .contains("eazyPortalCoreVersion  : $CORE_VERSION   (source: DSL override)")
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

}
