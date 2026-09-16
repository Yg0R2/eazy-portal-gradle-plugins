package org.eazyportal.gradle.dummysettings

import org.eazyportal.gradle.conventions.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.conventions.GradleUtils.runGradleTask
import org.eazyportal.gradle.conventions.project.ExampleProjectBuilder
import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.gradle.dummysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
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
 * which needs no `eazy-portal-core` repository to resolve —
 * keeping these tests hermetic while still exercising the auto-apply + version-resolution behavior under test here.
 */
class EazySettingsPluginFunctionalTest {

    @Test
    fun `every subproject gets eazy-project auto-applied, the root does not`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, subprojectNames = listOf("extras-a", "extras-b"))

        val output = runGradleTask(projectDir, ":extras-a:eazyDiagnostics", ":extras-b:eazyDiagnostics")

        assertThat(output)
            .contains("eazy-project diagnostics — :extras-a")
            .contains("eazy-project diagnostics — :extras-b")

        val rootOutput = runFailingGradleTask(projectDir, ":eazyDiagnostics")

        assertThat(rootOutput).contains("not found in root project")
    }

    @Test
    fun `eazy-project's own default applies when the DSL override is absent`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, subprojectNames = listOf("extras"))

        val output = runGradleTask(projectDir, ":extras:eazyDiagnostics")

        assertThat(output).contains("eazyPortalCoreVersion  : $EXAMPLE_CORE_DEFAULT_VERSION")
    }

    @Test
    fun `the eazySettings override wins over eazy-project's own default`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, subprojectNames = listOf("extras"), eazyPortalCoreVersionOverride = "9.9.9")

        val output = runGradleTask(projectDir, ":extras:eazyDiagnostics")

        assertThat(output).contains("eazyPortalCoreVersion  : 9.9.9")
    }

    @Test
    fun `CC double-run - override wins, the 2nd run reuses the CC entry, and zero CC problems occur`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, subprojectNames = listOf("extras-a", "extras-b"), eazyPortalCoreVersionOverride = "9.9.9")

        val firstRun = runGradleTask(
            projectDir,
            ":extras-a:eazyDiagnostics", ":extras-b:eazyDiagnostics",
            "--configuration-cache", "--configuration-cache-problems=fail",
        )
        assertThat(firstRun)
            .contains("Configuration cache entry stored.")
            .contains("eazyPortalCoreVersion  : 9.9.9")
            .doesNotContain("problem was found", "problems were found")

        val secondRun = runGradleTask(
            projectDir,
            ":extras-a:eazyDiagnostics", ":extras-b:eazyDiagnostics",
            "--configuration-cache", "--configuration-cache-problems=fail",
        )
        assertThat(secondRun)
            .contains("Reusing configuration cache.")
            .contains("eazyPortalCoreVersion  : 9.9.9")
            .doesNotContain("problem was found", "problems were found")
    }

    /**
     * A synthetic receiver: an empty root applying `org.eazyportal.gradle.eazy-settings` via the settings `plugins { }`
     * block (the real receiver bootstrap, README Quickstart) + one subproject per [subprojectNames], optionally
     * overriding `eazyPortalCoreVersion` via the `eazySettings { }` DSL.
     */
    private fun buildExampleProject(
        workingDir: File,
        subprojectNames: List<String>,
        eazyPortalCoreVersionOverride: String? = null,
    ): File =
        ExampleProjectBuilder(workingDir)
            .withRootProjectName("receiver")
            .withSettingsScript { generateBootstrapScript(eazyPortalCoreVersionOverride) }
            .apply { subprojectNames.forEach { withSubproject(it) } }
            .build()

    // Built with plain concatenation, not a nested trimIndent(): mixing raw-string indentation levels leaves stray
    // leading whitespace that trimIndent() can't reconcile across both blocks (same pitfall as
    // EazyProjectPluginFunctionalTest.writeArtifact).
    private fun generateBootstrapScript(eazyPortalCoreVersionOverride: String?): String =
        buildString {
            appendLine("plugins {")
            appendLine("    id(\"org.eazyportal.gradle.eazy-settings\")")
            appendLine("}")

            if (eazyPortalCoreVersionOverride != null) {
                appendLine()
                appendLine("$EAZY_SETTINGS_EXTENSION_NAME {")
                appendLine("    eazyPortalCoreVersion = \"$eazyPortalCoreVersionOverride\"")
                appendLine("}")
            }
        }

}
