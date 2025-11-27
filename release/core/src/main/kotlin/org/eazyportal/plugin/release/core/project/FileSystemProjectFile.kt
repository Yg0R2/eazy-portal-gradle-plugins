package org.eazyportal.plugin.release.core.project

import java.io.File
import java.nio.file.Path

data class FileSystemProjectFile(
    private val file: File
) : ProjectFile<File> {

    override fun createIfMissing() {
        file.createNewFile()
    }

    override fun exists(): Boolean =
        file.exists()

    override fun getFile(): File =
        file

    override fun getName(): String =
        file.name

    override fun getParent(): ProjectFile<File> =
        FileSystemProjectFile(file.parentFile)

    override fun getPath(): Path =
        file.toPath()

    override fun isDirectory(): Boolean =
        file.isDirectory

    override fun isFile(): Boolean =
        file.isFile

    override fun readLines(): List<String> =
        file.readLines()

    override fun readText(): String =
        file.readText()

    override fun resolve(subPath: String): FileSystemProjectFile =
        FileSystemProjectFile(file.resolve(subPath))

    override fun writeText(content: String) {
        file.writeText(content)
    }

    override fun toString(): String =
        file.absolutePath

}
