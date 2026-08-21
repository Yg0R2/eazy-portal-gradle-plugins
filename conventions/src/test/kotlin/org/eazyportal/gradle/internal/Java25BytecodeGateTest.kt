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

        // Scan-only for now (decided in TOOLS-59): the conventions build has no main sources yet
        // (ConventionsSupport arrives in TOOLS-60, the script plugins in TOOLS-61+), so an output
        // directory may legitimately be empty. Once sources exist, every class file found must be
        // major 69 — a clamped compile output fails.
        (javaClasses + kotlinClasses).forEach { classFile ->
            assertThat(ClassFileMajorVersionSupport.readMajorVersion(classFile))
                .describedAs("major version of %s", classFile)
                .isEqualTo(ClassFileMajorVersionSupport.JAVA_25_MAJOR_VERSION)
        }
    }

}
