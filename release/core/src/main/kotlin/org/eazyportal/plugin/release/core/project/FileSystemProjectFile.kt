package org.eazyportal.plugin.release.core.project

import java.io.File

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
        file.path

}
