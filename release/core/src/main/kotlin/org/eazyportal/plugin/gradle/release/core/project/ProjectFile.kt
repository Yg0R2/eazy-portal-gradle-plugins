package org.eazyportal.plugin.gradle.release.core.project

interface ProjectFile<T : Any> {

    fun createIfMissing()

    fun exists(): Boolean

    fun getFile(): T

    fun isDirectory(): Boolean

    fun isFile(): Boolean

    fun readLines(): List<String>

    fun readText(): String

    fun resolve(subPath: String): ProjectFile<T>

    fun writeText(content: String)

}
