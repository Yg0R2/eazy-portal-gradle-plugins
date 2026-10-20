package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for auto-applying `eazy-project` (design §6.3/§6.5) by `org.eazyportal.gradle.eazy-settings`:
 * a real Gradle build driven through TestKit's plugin-under-test classpath.
 * The subprojects are named outside the standard archetype set (design §5.2), so they fall back to the `default` archetype and stay hermetic.
 */
class EazySettingsAutoApplyFunctionalTest {

    @Test
    fun `every subproject gets eazy-project auto-applied, the root does not`(@TempDir workingDir: File) {
        val projectDir = exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)
            }

            subproject("extras-a")
            subproject("extras-b")
        }

        val gradleRunner = gradleRunner(projectDir)

        val output = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
        )

        assertThat(output)
            .contains("eazy-project diagnostics — :extras-a")
            .contains("eazy-project diagnostics — :extras-b")

        val rootOutput = gradleRunner.runFailingGradleTask(":$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(rootOutput).contains("not found in root project")
    }

}
