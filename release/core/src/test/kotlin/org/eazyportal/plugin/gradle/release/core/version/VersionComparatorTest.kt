package org.eazyportal.plugin.gradle.release.core.version

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.core.model.VersionFixtures
import org.eazyportal.plugin.gradle.release.core.version.model.Version
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class VersionComparatorTest {

    private val underTest = VersionComparator()

    @MethodSource("compare")
    @ParameterizedTest
    fun test_compare(version1: Version?, version2: Version?, expected: Int) {
        // GIVEN
        // WHEN
        val actual = underTest.compare(version1, version2)

        // THEN
        assertThat(actual).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun compare() = listOf(
            Arguments.of(VersionFixtures.RELEASE_001, null, -1),
            Arguments.of(null, null, 0),
            Arguments.of(null, VersionFixtures.RELEASE_001, 1),

            Arguments.of(VersionFixtures.RELEASE_001, VersionFixtures.SNAPSHOT_001, -1),

            Arguments.of(VersionFixtures.RELEASE_001, VersionFixtures.RELEASE_002, -1),
            Arguments.of(VersionFixtures.RELEASE_001, VersionFixtures.RELEASE_001, 0),
            Arguments.of(VersionFixtures.RELEASE_002, VersionFixtures.RELEASE_001, 1),

            Arguments.of(VersionFixtures.RELEASE_010, VersionFixtures.RELEASE_020, -1),
            Arguments.of(VersionFixtures.RELEASE_010, VersionFixtures.RELEASE_010, 0),
            Arguments.of(VersionFixtures.RELEASE_020, VersionFixtures.RELEASE_010, 1),

            Arguments.of(VersionFixtures.RELEASE_100, VersionFixtures.RELEASE_200, -1),
            Arguments.of(VersionFixtures.RELEASE_100, VersionFixtures.RELEASE_100, 0),
            Arguments.of(VersionFixtures.RELEASE_200, VersionFixtures.RELEASE_100, 1),

            Arguments.of(VersionFixtures.RELEASE_100, VersionFixtures.RELEASE_001, 1),
            Arguments.of(VersionFixtures.RELEASE_001, VersionFixtures.RELEASE_100, -1),

            Arguments.of(VersionFixtures.RELEASE_100, Version(1, 0, 0, "1"), -1),
            Arguments.of(Version(1, 0, 0, "1"), VersionFixtures.RELEASE_100, 1),

            Arguments.of(Version(1, 0, 0, "1.1"), Version(1, 0, 0, "1.1.1"), -1),
            Arguments.of(Version(1, 0, 0, "1.1"), Version(1, 0, 0, "1.1"), 0),
            Arguments.of(Version(1, 0, 0, "1.1.1"), Version(1, 0, 0, "1.1"), 1),

            Arguments.of(Version(1, 0, 0, "1"), Version(1, 0, 0, "alpha"), -1),
            Arguments.of(Version(1, 0, 0, "1.1"), Version(1, 0, 0, "alpha"), -1),
            Arguments.of(Version(1, 0, 0, "alpha"), Version(1, 0, 0, "alpha"), 0),
            Arguments.of(Version(1, 0, 0, "alpha"), Version(1, 0, 0, "1"), 1),
            Arguments.of(Version(1, 0, 0, "alpha"), Version(1, 0, 0, "1.1"), 1),

            Arguments.of(Version(1, 0, 0, "alpha"), Version(1, 0, 0, "beta"), -1),
            Arguments.of(Version(1, 0, 0, "beta"), Version(1, 0, 0, "alpha"), 1),

            Arguments.of(Version(1, 0, 0, "alpha.1"), Version(1, 0, 0, "alpha.2"), -1),
            Arguments.of(Version(1, 0, 0, "alpha.1"), Version(1, 0, 0, "alpha.1"), 0),
            Arguments.of(Version(1, 0, 0, "alpha.2"), Version(1, 0, 0, "alpha.1"), 1),

            // Build metadata does not figure into precedence during comparison of versions
            Arguments.of(Version(1, 0, 0, "1", "1"), Version(1, 0, 0, "1", "2"), 0),
            Arguments.of(Version(1, 0, 0, "1", "1"), Version(1, 0, 0, "1", "1"), 0),
            Arguments.of(Version(1, 0, 0, "1", "2"), Version(1, 0, 0, "1", "1"), 0),

            Arguments.of(Version(1, 0, 0, "1", "1.1"), Version(1, 0, 0, "1", "1"), 0),
            Arguments.of(Version(1, 0, 0, "1", "1.1"), Version(1, 0, 0, "1", "1.1"), 0),
            Arguments.of(Version(1, 0, 0, "1", "1"), Version(1, 0, 0, "1", "1.1"), 0)
        )
    }

}
