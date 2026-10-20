package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for `eazySettings { exclude }` — subprojects left completely unconfigured:
 * real Gradle builds driven through TestKit's plugin-under-test classpath.
 * The subprojects are named outside the standard archetype set (design §5.2), so they fall back to the `default` archetype and stay hermetic.
 */
class EazySettingsExcludeFunctionalTest {

    @Test
    fun `an excluded subproject is left unconfigured, so it can apply java-platform, while the others still get eazy-project`(
        @TempDir workingDir: File,
    ) {
        val projectDir = exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)

                script {
                    """
                    $EAZY_SETTINGS_EXTENSION_NAME {
                        exclude = listOf("platform")
                    }
                    """.trimIndent()
                }
            }

            subproject("extras")
            subproject("platform") {
                plugins("java-platform")
            }
        }

        val gradleRunner = gradleRunner(projectDir)

        gradleRunner.runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME").run {
            assertThat(this).contains("eazy-project diagnostics — :extras")
        }

        gradleRunner.runFailingGradleTask(":platform:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME").run {
            assertThat(this).contains("not found in project ':platform'")
        }
    }

    @Test
    fun `a subproject applying java-platform fails when it is not excluded`(@TempDir workingDir: File) {
        val projectDir = exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)
            }

            subproject("platform") {
                plugins("java-platform")
            }
        }

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("help")

        assertThat(output).contains("""The "java-platform" plugin cannot be applied together with the "java" (or "java-library") plugin.""")
    }

}
