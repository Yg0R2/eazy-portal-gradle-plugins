package org.eazyportal.gradle.internal

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.walk

internal object ClassFileMajorVersionSupport {
    const val JAVA_25_MAJOR_VERSION = 69

    fun readMajorVersion(classFile: Path): Int {
        val bytes = classFile.readBytes()
        require(bytes.size >= 8) { "Not a class file: $classFile" }
        require(bytes[0] == 0xCA.toByte() && bytes[1] == 0xFE.toByte() &&
            bytes[2] == 0xBA.toByte() && bytes[3] == 0xBE.toByte()) {
            "Not a class file: $classFile"
        }
        return ((bytes[6].toInt() and 0xFF) shl 8) or (bytes[7].toInt() and 0xFF)
    }

    fun classFilesUnder(directory: Path): List<Path> =
        if (Files.exists(directory)) {
            directory.walk()
                .filter { it.isRegularFile() && it.fileName.toString().endsWith(".class") }
                .toList()
        } else {
            emptyList()
        }
}
