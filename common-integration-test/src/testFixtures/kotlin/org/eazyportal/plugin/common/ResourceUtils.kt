package org.eazyportal.plugin.common

import java.io.File

object ResourceUtils {

    private const val COMMON_FOLDER_NAME = "_common"

    fun File.copyIntoFromResources(
        parentFolder: String = COMMON_FOLDER_NAME,
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
    ): File {
        val resourceFullPath = "$parentFolder/$resourcePath"
        val resourceCommonPath = "$COMMON_FOLDER_NAME/$resourcePath"

        return with(this@ResourceUtils::class.java.classLoader) {
            getResource(resourceFullPath) ?: getResource(resourceCommonPath)
        }?.let { File(it.toURI()) }
            ?: if (parentFolder == COMMON_FOLDER_NAME) {
                throw IllegalArgumentException("Resource not found in $resourceFullPath")
            } else {
                throw IllegalArgumentException("Resource not found in $resourceFullPath, $resourceCommonPath")
            }
    }

}
