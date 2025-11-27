package org.eazyportal.plugin.release.core.project

import java.nio.file.Path

interface ProjectFile<T : Any> {

    fun createIfMissing()

    fun exists(): Boolean

    fun getFile(): T

    fun getName(): String

    fun getParent(): ProjectFile<T>

    fun getPath(): Path

    fun isDirectory(): Boolean

    fun isFile(): Boolean

    fun readLines(): List<String>

    fun readText(): String

    fun resolve(subPath: String): ProjectFile<T>

    fun writeText(content: String)

}
