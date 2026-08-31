package org.eazyportal.gradle.internal

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.eazyportal.gradle.internal.assertion.PomAssert.Companion.assertThatHasEazyPortalValues
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.absolutePathString

class CoordinatesAndPublishingTest {

    @Test
    fun `group and version originate from root gradle properties`() {
        val actual = runGradleTask(PROJECT_DIR, "properties")

        // conventions/settings.gradle.kts keeps the root group and appends ".conventions" (decided
        // in TOOLS-59; the design's substitution coordinates are adjusted accordingly later).
        assertThat(actual)
            .contains("group: $CONVENTIONS_PLUGIN_GROUP_ID")
            .contains("version: $CONVENTIONS_PLUGIN_VERSION")
    }

    @Test
    fun `command-line version property overrides gradle properties`() {
        val version = "2.3.4-DUMMY"

        val actual = runGradleTask(PROJECT_DIR,  "properties", "-Pversion=$version")

        assertThat(actual)
            .contains("group: $CONVENTIONS_PLUGIN_GROUP_ID")
            .contains("version: $version")
    }

    @Test
    fun `publishing release version should fail without credentials`() {
        val actual = runFailingGradleTask(
            PROJECT_DIR,
            "publish",
            "-Pversion=0.0.1",
        )

        assertThat(actual)
            .contains("The following Gradle properties are missing for 'GitHubPackages' credentials:")
            .contains("- GitHubPackagesUsername")
            .contains("- GitHubPackagesPassword")
    }

    @Test
    fun `publishing SNAPSHOT to a file repository produces a non-bare POM`(@TempDir workingDir: Path) {
        runGradleTask(
            PROJECT_DIR,
            "publish",
            "-Dmaven.repo.local=${workingDir.absolutePathString()}",
        )

        // Non-bare POM, precisely: license + scm fields and coordinates (design §4.1).
        assertThatHasEazyPortalValues(findPublishedConventionsPom(workingDir)) {
            groupId = CONVENTIONS_PLUGIN_GROUP_ID
            artifactId = "conventions"
            version = CONVENTIONS_PLUGIN_VERSION
            name = "conventions"
            description = "EazyPortal Gradle convention plugins"
        }
    }

    /** All POMs published into a (temp) repository directory, but there should be only one for the `conventions` project. */
    private fun findPublishedConventionsPom(repoDir: Path): Path =
        Files.walk(repoDir)
            .filter { it.fileName.toString().equals("conventions-$CONVENTIONS_PLUGIN_VERSION.pom", true) }
            .toList()
            .also { assertThat(it).hasSize(1) }
            .single()

    companion object {
        /** The conventions project dir — the Test task's working directory. */
        private val PROJECT_DIR = File("")

        /** The ROOT gradle.properties — the single source of truth for group/version (design §2). */
        private val ROOT_PROPERTIES = Properties().apply {
            PROJECT_DIR.resolve("../gradle.properties")
                .inputStream()
                .use { load(it) }
        }

        /** The conventions build's group (root group + ".conventions" — conventions/settings.gradle.kts). */
        private val CONVENTIONS_PLUGIN_GROUP_ID = "${ROOT_PROPERTIES.getProperty("group")}.conventions"
            .also { assertThat(it).isEqualTo("org.eazyportal.gradle.conventions") }

        /** The conventions build's version (same as root version). */
        private val CONVENTIONS_PLUGIN_VERSION = ROOT_PROPERTIES.getProperty("version")
    }

}
