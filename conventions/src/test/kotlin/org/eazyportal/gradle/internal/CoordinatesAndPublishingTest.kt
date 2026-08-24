package org.eazyportal.gradle.internal

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.absolutePathString

class CoordinatesAndPublishingTest {

    @Test
    fun `group and version originate from root gradle properties`() {
        val actual = runGradleTask(PROJECT_DIR, "properties")

        // conventions/settings.gradle.kts keeps the root group and appends ".conventions" (decided
        // in TOOLS-59; the design's substitution coordinates are adjusted accordingly later).
        assertThat(actual)
            .contains("group: $GROUP_ID")
            .contains("version: $VERSION")
    }

    @Test
    fun `command-line version property overrides gradle properties`() {
        val version = "2.3.4-DUMMY"

        val actual = runGradleTask(PROJECT_DIR,  "properties", "-Pversion=$version")

        assertThat(actual)
            .contains("group: $GROUP_ID")
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

        val document = parsePom(findPublishedConventionsPom(workingDir))

        assertThat(document.getElementsByTagName("license").length).isGreaterThan(0)
        assertThat(document.getElementsByTagName("scm").length).isGreaterThan(0)

        // Non-bare POM, precisely: license + scm fields and coordinates (design §4.1).
        assertThat(textOf(document, "name")).isEqualTo("conventions")
        assertThat(textOf(document, "url")).isEqualTo("https://github.com/«github-owner»/«github-repo»")
        assertThat(textOf(document, "licenses/license/name")).isEqualTo("«license-name»") // resolved in TOOLS-76
        assertThat(textOf(document, "licenses/license/url"))
            .isEqualTo("https://raw.githubusercontent.com/«github-owner»/«github-repo»/refs/heads/main/LICENSE")
        assertThat(textOf(document, "scm/connection"))
            .isEqualTo("scm:git:https://github.com/«github-owner»/«github-repo».git")
        assertThat(textOf(document, "scm/developerConnection"))
            .isEqualTo("scm:git:ssh://git@github.com/«github-owner»/«github-repo».git")
        assertThat(textOf(document, "scm/url")).isEqualTo("https://github.com/«github-owner»/«github-repo»")
        assertThat(textOf(document, "groupId")).isEqualTo(GROUP_ID)
        assertThat(textOf(document, "artifactId")).isEqualTo("conventions")
        assertThat(textOf(document, "version")).isEqualTo(VERSION)
    }

    /** All POMs published into a (temp) repository directory, but there should be only one for the `conventions` project. */
    private fun findPublishedConventionsPom(repoDir: Path): Path =
        Files.walk(repoDir)
            .filter { it.fileName.toString().equals("conventions-$VERSION.pom", true) }
            .toList()
            .also { assertThat(it).hasSize(1) }
            .single()

    private fun parsePom(pom: Path): Document =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(pom.toFile())

    /** First text content of the (slash-separated) element path, or null if absent. */
    private fun textOf(document: Document, path: String): String? =
        path.split('/')
            .fold(listOf<Node>(document.documentElement)) { parents, child ->
                parents.flatMap { parent ->
                    (0 until parent.childNodes.length)
                        .map { parent.childNodes.item(it) }
                        .filter { (it is Element) && (it.tagName == child) }
                }
            }.firstOrNull()
                ?.textContent
                ?.trim()

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
        private val GROUP_ID = "${ROOT_PROPERTIES.getProperty("group")}.conventions"
            .also { assertThat(it).isEqualTo("org.eazyportal.gradle.conventions") }

        /** The conventions build's version (same as root version). */
        private val VERSION = ROOT_PROPERTIES.getProperty("version")
    }

}
