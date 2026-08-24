package org.eazyportal.gradle.conventions.internal

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ConventionsUtilsTest {

    @TestFactory
    fun `kotlinLanguageVersion maps version string to kotlin language version`() =
        listOf(
            Pair("2.1.0", KotlinVersion.KOTLIN_2_1),
            Pair("2.3.21", KotlinVersion.KOTLIN_2_3),
            // Pair("2.4", KotlinVersion.KOTLIN_2_4), TODO: TOOLS-77: proper SemVer validation, 2-part input (design §4.5/D13)
        ).map { (version, kotlinVersion) ->
            dynamicTest("$version to KotlinVersion.${kotlinVersion.name}") {
                assertThat(ConventionsUtils.kotlinLanguageVersion(version)).isEqualTo(kotlinVersion)
            }
        }

    @Test
    fun `isRelease is true for a release version`() {
        assertThat(ConventionsUtils.isRelease("1.2.3")).isTrue()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "1.2.3-SNAPSHOT",
            "",
            " ",
            "unspecified",
        ]
    )
    fun `isRelease is false for`(version: String) {
        assertThat(ConventionsUtils.isRelease(version)).isFalse()
    }

}
