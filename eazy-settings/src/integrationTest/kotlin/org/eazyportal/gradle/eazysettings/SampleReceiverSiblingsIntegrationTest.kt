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
 * TOOLS-73 (design §5.4, §7.4/§7.5): a receiver only `include`s the modules it needs —
 * an optional sibling is wired when present and skipped when absent, while a missing required sibling fails the build.
 * Only the `eazyDiagnostics` wiring report is read, so no dependency is resolved and no repository is needed.
 */
class SampleReceiverSiblingsIntegrationTest {

    @Test
    fun `a receiver with only the required siblings wires just those and skips the absent optional ones`(
        @TempDir workingDir: File,
    ) {
        // `client` and `web` each require `api`; every other sibling (`common`, `service`, ...) is optional and absent here.
        val projectDir = buildExampleProject(workingDir, listOf("api", "client", "web"))

        val output = gradleRunner(projectDir)
            .runGradleTask(EAZY_PROJECT_DIAGNOSTICS_TASK_NAME)

        assertThat(output)
            .containsSubsequence(
                "eazy-project diagnostics — :api",
                "  siblings               : []",
            ).containsSubsequence(
                "eazy-project diagnostics — :client",
                "  siblings               : [:api (api)]",
            ).containsSubsequence(
                "eazy-project diagnostics — :web",
                "  siblings               : [:api (implementation)]",
            )
    }

    @Test
    fun `a receiver fails when a module is missing its required sibling`(
        @TempDir workingDir: File,
    ) {
        // `client` requires `api`, which this receiver does not have.
        val projectDir = buildExampleProject(workingDir, listOf("common", "client"))

        val output = gradleRunner(projectDir)
            .runFailingGradleTask(EAZY_PROJECT_DIAGNOSTICS_TASK_NAME)

        assertThat(output).containsSubsequence(
            "A problem occurred configuring project ':client'.",
            "   > Project with path ':api' could not be found in project ':client'.",
        )
    }

    private fun buildExampleProject(
        workingDir: File,
        projectNames: List<String>,
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)
            }

            projectNames.forEach {
                subproject(it)
            }
        }

}
