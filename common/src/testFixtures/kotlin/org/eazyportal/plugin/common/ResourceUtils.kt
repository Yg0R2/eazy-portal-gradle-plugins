package org.eazyportal.plugin.common

import java.io.File

object ResourceUtils {

    fun File.copyIntoFromResources(
        parentFolder: String = "_common",
        resourcePath: String,
    ): File {
        val resourceFile = getResourceFile(parentFolder, resourcePath)

        return resolve(resourcePath).also {
            it.parentFile.mkdirs()

            if (resourceFile.isDirectory) {
                resourceFile.copyRecursively(it, true)
            } else {
                it.writeText(resourceFile.readText())
            }
        }
    }

    private fun getResourceFile(
        parentFolder: String,
        resourcePath: String,
    ): File =
        with(this@ResourceUtils::class.java) {
            classLoader.getResource("$parentFolder/$resourcePath")
                ?: classLoader.getResource("_common/$resourcePath")
        }?.let { File(it.toURI()) }
            ?: throw IllegalArgumentException(
                "Resource not found in $parentFolder/$resourcePath, _common/$resourcePath"
            )

}
