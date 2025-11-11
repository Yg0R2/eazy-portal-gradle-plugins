package org.eazyportal.plugin.release.core.version

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_002
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_010
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_020
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_100
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_200
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_010
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_100
import org.eazyportal.plugin.release.core.version.exception.InvalidVersionException
import org.eazyportal.plugin.release.core.version.model.Version
import org.eazyportal.plugin.release.core.version.model.VersionIncrement
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ReleaseVersionProviderTest {

    private val underTest = ReleaseVersionProvider()

    @MethodSource("provide")
    @ParameterizedTest
    fun test_provide(version: Version, versionIncrement: VersionIncrement, expected: Version) {
        // GIVEN
        // WHEN
        val actual = underTest.provide(version, versionIncrement)

        // THEN
        assertThat(actual).isEqualTo(expected)
    }

    @TestFactory
    fun test_provide_shouldFail(): List<DynamicTest> =
        listOf(
            RELEASE_001,
            SNAPSHOT_001,
        ).flatMap { version ->
            listOf(
                VersionIncrement.ERROR,
                VersionIncrement.NONE,
            ).map { versionIncrement ->
                DynamicTest.dynamicTest("with '${versionIncrement.name}' version increment for version: $version") {
                    // GIVEN
                    // WHEN & THEN
                    assertThatThrownBy {
                        underTest.provide(version, versionIncrement)
                    }.isInstanceOf(InvalidVersionException::class.java)
                        .hasMessage("Cannot provide release version with '${versionIncrement.name}' version increment for version: $version")
                }
            }
        }

    companion object {
        @JvmStatic
        private fun provide(): List<Arguments> =
            listOf(
                Arguments.of(SNAPSHOT_001, VersionIncrement.PATCH, RELEASE_001),
                Arguments.of(SNAPSHOT_001, VersionIncrement.MINOR, RELEASE_010),
                Arguments.of(SNAPSHOT_001, VersionIncrement.MAJOR, RELEASE_100),

                Arguments.of(SNAPSHOT_010, VersionIncrement.PATCH, RELEASE_010),
                Arguments.of(SNAPSHOT_010, VersionIncrement.MINOR, RELEASE_020),
                Arguments.of(SNAPSHOT_010, VersionIncrement.MAJOR, RELEASE_100),

                Arguments.of(SNAPSHOT_100, VersionIncrement.PATCH, RELEASE_100),
                Arguments.of(SNAPSHOT_100, VersionIncrement.MINOR, Version(1, 1, 0)),
                Arguments.of(SNAPSHOT_100, VersionIncrement.MAJOR, RELEASE_200),

                Arguments.of(RELEASE_001, VersionIncrement.PATCH, RELEASE_002),
                Arguments.of(RELEASE_001, VersionIncrement.MINOR, RELEASE_010),
                Arguments.of(RELEASE_001, VersionIncrement.MAJOR, RELEASE_100),

                Arguments.of(Version(0, 0, 1, null, "invalid.version"), VersionIncrement.PATCH, RELEASE_001),
                Arguments.of(Version(0, 0, 1, "SNAPSHOT", null), VersionIncrement.PATCH, RELEASE_001)
            )
    }

}
