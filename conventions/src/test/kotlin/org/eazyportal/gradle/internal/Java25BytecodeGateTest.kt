package org.eazyportal.gradle.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.walk

class Java25BytecodeGateTest {

    @Test
    fun `compileJava and compileKotlin emit class-file major version 69`() {
        val projectDir = File("").toPath()
        val javaClasses = classFilesUnder(projectDir.resolve("build/classes/java/main"))
        val kotlinClasses = classFilesUnder(projectDir.resolve("build/classes/kotlin/main"))

        // Kotlin main sources exist (ConventionsSupport since TOOLS-60, the script plugins in TOOLS-61+),
        // so the Kotlin compile output must be non-empty and every class file major 69 —
        // a clamped compile output fails. Java main stays scan-only:
        // this build is Kotlin-only, so that directory is legitimately empty unless Java sources ever appear.
        assertThat(kotlinClasses).isNotEmpty
        (javaClasses + kotlinClasses).forEach { classFile ->
            assertThat(readMajorVersion(classFile))
                .describedAs("major version of %s", classFile)
                .isEqualTo(JAVA_25_MAJOR_VERSION)
        }
    }

    companion object {
        private const val JAVA_25_MAJOR_VERSION = 69

        private fun readMajorVersion(classFile: Path): Int {
            val bytes = classFile.readBytes().also {
                require(it.size >= 8) { "Not a class file: $classFile" }
                require((it[0] == 0xCA.toByte()) && (it[1] == 0xFE.toByte()) && (it[2] == 0xBA.toByte()) && (it[3] == 0xBE.toByte())) {
                    "Not a class file: $classFile"
                }
            }

            return ((bytes[6].toInt() and 0xFF) shl 8) or (bytes[7].toInt() and 0xFF)
        }

        private fun classFilesUnder(directory: Path): List<Path> =
            if (Files.exists(directory)) {
                directory.walk()
                    .filter { it.isRegularFile() && it.fileName.toString().endsWith(".class") }
                    .toList()
            } else {
                emptyList()
            }
    }

}
