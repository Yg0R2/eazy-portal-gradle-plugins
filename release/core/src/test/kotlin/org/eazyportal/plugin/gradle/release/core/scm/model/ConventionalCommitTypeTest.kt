package org.eazyportal.plugin.gradle.release.core.scm.model

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.gradle.release.core.version.model.VersionIncrement.PATCH
import org.junit.jupiter.api.Test

class ConventionalCommitTypeTest {

    @Test
    fun test_constructor_shouldFail_whenThereAreNoAliases() {
        // GIVEN
        // WHEN & THEN
        assertThatThrownBy { ConventionalCommitType(listOf(), PATCH) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Required to have at least 1 alias for each type.")
    }

}
