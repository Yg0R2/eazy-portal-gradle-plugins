package org.eazyportal.gradle.conventions.assertion

import org.eazyportal.gradle.conventions.extension.textOf
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_GROUP_ID
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_SNAPSHOT_VERSION
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
            hasTextAt("licenses/license/name", "Apache-2.0")
            hasTextAt("licenses/license/url", "https://raw.githubusercontent.com/eazyportal/gradle-plugins/refs/heads/main/LICENSE")

            Assertions.assertThat(actual.getElementsByTagName("scm").length).isGreaterThan(0)
            hasTextAt("scm/connection", "scm:git:https://github.com/eazyportal/gradle-plugins.git")
            hasTextAt("scm/developerConnection", "scm:git:ssh://git@github.com/eazyportal/gradle-plugins.git")
            hasTextAt("scm/url", "https://github.com/eazyportal/gradle-plugins")

            hasTextAt("url", "https://github.com/eazyportal/gradle-plugins")
        }

    /** The first element at the slash-separated element [xPath] has the [expectedValue] as text. */
    fun hasTextAt(xPath: String, expectedValue: String): PomAssert =
        also { it.xPathTextIsEqualTo(xPath, expectedValue) }

    fun isArtifactIdEqualTo(expectedArtifactId: String): PomAssert =
        hasTextAt("artifactId", expectedArtifactId)

    fun isDescriptionEqualTo(expectedDescription: String): PomAssert =
        hasTextAt("description", expectedDescription)

    fun isGroupIdEqualTo(expectedGroupId: String): PomAssert =
        hasTextAt("groupId", expectedGroupId)

    fun isNameEqualTo(expectedName: String): PomAssert =
        hasTextAt("name", expectedName)

    fun isPackagingEqualTo(expectedPackaging: String): PomAssert =
        hasTextAt("packaging", expectedPackaging)

    fun isVersionEqualTo(expectedVersion: String): PomAssert =
        hasTextAt("version", expectedVersion)

    private fun xPathTextIsEqualTo(xPath: String, expectedValue: String) {
        val actualValue = actual.textOf(xPath)

        Assertions.assertThat(actualValue)
            .withFailMessage("Expected value at XPath '$xPath' to be '$expectedValue', but was '$actualValue'")
            .isEqualTo(expectedValue)
    }

    data class PomAssertValues(
        var groupId: String = EXAMPLE_PROJECT_GROUP_ID,
        var artifactId: String = EXAMPLE_ROOT_PROJECT_NAME,
        var version: String = EXAMPLE_PROJECT_SNAPSHOT_VERSION,
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
