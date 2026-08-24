package org.eazyportal.gradle.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

class Java25BytecodeGateTest {

    @Test
    fun `compileJava and compileKotlin emit class-file major version 69`() {
        val projectDir = Path.of(System.getProperty("user.dir"))
        val javaClasses = ClassFileMajorVersionSupport.classFilesUnder(
            projectDir.resolve("build/classes/java/main"),
        )
        val kotlinClasses = ClassFileMajorVersionSupport.classFilesUnder(
            projectDir.resolve("build/classes/kotlin/main"),
        )

        // Kotlin main sources exist (ConventionsSupport since TOOLS-60, the script plugins in
        // TOOLS-61+), so the Kotlin compile output must be non-empty and every class file major 69 —
        // a clamped compile output fails. Java main stays scan-only: this build is Kotlin-only, so
        // that directory is legitimately empty unless Java sources ever appear.
        assertThat(kotlinClasses).isNotEmpty
        (javaClasses + kotlinClasses).forEach { classFile ->
            assertThat(ClassFileMajorVersionSupport.readMajorVersion(classFile))
                .describedAs("major version of %s", classFile)
                .isEqualTo(ClassFileMajorVersionSupport.JAVA_25_MAJOR_VERSION)
        }
    }

}
