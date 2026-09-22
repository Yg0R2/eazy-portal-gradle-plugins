package org.eazyportal.gradle.conventions.assertion

import org.eazyportal.gradle.conventions.extension.textOf
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_GROUP_ID
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_DESCRIPTION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_NAME
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions
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
        var groupId: String = EXAMPLE_PROJECT_GROUP_ID,
        var artifactId: String = EXAMPLE_ROOT_PROJECT_NAME,
        var version: String = EXAMPLE_PROJECT_VERSION,
        var name: String = EXAMPLE_ROOT_PROJECT_NAME,
        var description: String = EXAMPLE_ROOT_PROJECT_DESCRIPTION,
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
