package org.eazyportal.gradle.internal

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.absolutePathString

class CoordinatesAndPublishingTest {

    private val conventionsProjectDir: File = File("")

    @Test
    fun `group and version originate from root gradle properties`() {
        val output = runGradle("properties")

        // conventions/settings.gradle.kts keeps the root group and appends ".conventions" (decided
        // in TOOLS-59; the design's substitution coordinates are adjusted accordingly later).
        assertThat(output).contains("group: org.eazyportal.gradle.conventions")
        assertThat(output).contains("version: 0.1.0-SNAPSHOT")
    }

    @Test
    fun `command-line version property overrides gradle properties`() {
        val output = runGradle("properties", "-Pversion=2.3.4")

        assertThat(output).contains("group: org.eazyportal.gradle.conventions")
        assertThat(output).contains("version: 2.3.4")
    }

    @Test
    fun `publishing to a file repository produces a non-bare POM`(@TempDir tempDir: Path) {
        val repoDir = tempDir.resolve("repository")
        Files.createDirectories(repoDir)

        runGradle(
            "publish",
            "-PtestPublishRepo=${repoDir.toUri()}",
            "-Dmaven.repo.local=${tempDir.resolve("m2").absolutePathString()}",
        )

        val pom = findPublishedPom(repoDir)
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.toFile())

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
        assertThat(textOf(document, "groupId")).isEqualTo("org.eazyportal.gradle.conventions")
        assertThat(textOf(document, "artifactId")).isEqualTo("conventions")
        assertThat(textOf(document, "version")).isEqualTo("0.1.0-SNAPSHOT")
    }

    private fun runGradle(vararg arguments: String): String {
        val result = GradleRunner.create()
            .withProjectDir(conventionsProjectDir)
            .withArguments(*arguments)
            .forwardOutput()
            .build()

        assertThat(result.output).isNotBlank
        return result.output
    }

    private fun findPublishedPom(repoDir: Path): Path {
        val poms = Files.walk(repoDir)
            .filter { it.fileName.toString().endsWith(".pom") }
            .toList()

        assertThat(poms).isNotEmpty
        return poms.single()
    }

    /** First text content of the (slash-separated) element path, or null if absent. */
    private fun textOf(document: Document, path: String): String? {
        val elements = path.split('/').fold(listOf<Node>(document.documentElement)) { parents, child ->
            parents.flatMap { parent ->
                (0 until parent.childNodes.length)
                    .map { parent.childNodes.item(it) }
                    .filter { it is Element && it.tagName == child }
            }
        }
        return elements.firstOrNull()?.textContent?.trim()
    }
}
