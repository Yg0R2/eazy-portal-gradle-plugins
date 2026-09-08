package org.eazyportal.gradle.conventions.assertion

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions
import org.eazyportal.gradle.conventions.extension.textOf
import org.eazyportal.gradle.conventions.project.ExampleProjectFixtures.ARTIFACT_ID
import org.eazyportal.gradle.conventions.project.ExampleProjectFixtures.DESCRIPTION
import org.eazyportal.gradle.conventions.project.ExampleProjectFixtures.GROUP_ID
import org.eazyportal.gradle.conventions.project.ExampleProjectFixtures.VERSION
import org.w3c.dom.Document
import java.io.File
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class PomAssert(
    pomFile: File,
) : AbstractObjectAssert<PomAssert, Document>(
    parsePom(pomFile),
    PomAssert::class.java,
) {

    constructor(pomPath: Path) : this(pomPath.toFile())

    fun hasEazyPortalValues(): PomAssert =
        also {
            Assertions.assertThat(actual.getElementsByTagName("license").length).isGreaterThan(0)
            it.xPathTextIsEqualTo("licenses/license/name", "«license-name»") // resolved in TOOLS-76
            it.xPathTextIsEqualTo("licenses/license/url", "https://raw.githubusercontent.com/«github-owner»/«github-repo»/refs/heads/main/LICENSE")

            Assertions.assertThat(actual.getElementsByTagName("scm").length).isGreaterThan(0)
            it.xPathTextIsEqualTo("scm/connection", "scm:git:https://github.com/«github-owner»/«github-repo».git")
            it.xPathTextIsEqualTo("scm/developerConnection", "scm:git:ssh://git@github.com/«github-owner»/«github-repo».git")
            it.xPathTextIsEqualTo("scm/url", "https://github.com/«github-owner»/«github-repo»")

            it.xPathTextIsEqualTo("url", "https://github.com/«github-owner»/«github-repo»")
        }

    fun isArtifactIdEqualTo(expectedArtifactId: String): PomAssert =
        also { xPathTextIsEqualTo("artifactId", expectedArtifactId) }

    fun isDescriptionEqualTo(expectedDescription: String): PomAssert =
        also { xPathTextIsEqualTo("description", expectedDescription) }

    fun isGroupIdEqualTo(expectedGroupId: String): PomAssert =
        also { xPathTextIsEqualTo("groupId", expectedGroupId) }

    fun isNameEqualTo(expectedName: String): PomAssert =
        also { xPathTextIsEqualTo("name", expectedName) }

    fun isVersionEqualTo(expectedVersion: String): PomAssert =
        also { xPathTextIsEqualTo("version", expectedVersion) }

    private fun xPathTextIsEqualTo(xPath: String, expectedValue: String) {
        val actualValue = actual.textOf(xPath)

        Assertions.assertThat(actualValue)
            .withFailMessage("Expected value at XPath '$xPath' to be '$expectedValue', but was '$actualValue'")
            .isEqualTo(expectedValue)
    }

    data class PomAssertValues(
        var groupId: String = GROUP_ID,
        var artifactId: String = ARTIFACT_ID,
        var version: String = VERSION,
        var name: String = ARTIFACT_ID,
        var description: String = DESCRIPTION,
    )

    companion object {
        fun assertThat(
            pom: File,
            assertValuesFunction: PomAssertValues.() -> Unit = { },
        ): PomAssert {
            val assertValues = PomAssertValues().apply {
                assertValuesFunction()
            }

            return assertThat(pom, assertValues)
        }

        fun assertThat(
            pom: Path,
            assertValuesFunction: PomAssertValues.() -> Unit = { },
        ): PomAssert =
            assertThat(pom.toFile(), assertValuesFunction)

        fun assertThat(
            pom: File,
            assertValues: PomAssertValues,
        ): PomAssert =
            PomAssert(pom)
                .isGroupIdEqualTo(assertValues.groupId)
                .isArtifactIdEqualTo(assertValues.artifactId)
                .isVersionEqualTo(assertValues.version)
                .isNameEqualTo(assertValues.name)
                .isDescriptionEqualTo(assertValues.description)

        fun assertThatHasEazyPortalValues(
            pom: File,
            assertValuesFunction: PomAssertValues.() -> Unit = { },
        ): PomAssert =
            assertThat(pom, assertValuesFunction)
                .hasEazyPortalValues()

        fun assertThatHasEazyPortalValues(
            pom: Path,
            assertValuesFunction: PomAssertValues.() -> Unit = { },
        ): PomAssert =
            assertThat(pom, assertValuesFunction)
                .hasEazyPortalValues()

        private fun parsePom(pom: File): Document =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(pom)
    }

}
