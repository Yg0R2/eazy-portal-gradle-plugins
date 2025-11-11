package org.eazyportal.plugin.release.core.version

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.version.exception.InvalidVersionException
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.Test

class SnapshotVersionProviderTest {

    private val underTest = SnapshotVersionProvider()

    @Test
    fun test_provide() {
        // GIVEN
        // WHEN
        val actual = underTest.provide(RELEASE_001)

        // THEN
        assertThat(actual).isEqualTo(SNAPSHOT_002)
    }

    @Test
    fun test_provide_shouldFail_whenSnapshotVersionProvided() {
        // GIVEN
        // WHEN & THEN
        assertThatThrownBy {
            underTest.provide(SNAPSHOT_002)
        }.isInstanceOf(InvalidVersionException::class.java)
            .hasMessage("Project already on ${Version.DEVELOPMENT_VERSION_SUFFIX} version.")
    }

}
