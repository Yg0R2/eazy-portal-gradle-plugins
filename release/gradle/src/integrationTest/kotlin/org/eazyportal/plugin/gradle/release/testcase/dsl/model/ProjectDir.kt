package org.eazyportal.plugin.gradle.release.testcase.dsl.model

import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import java.io.File

data class ProjectDir(
    val localDir: File,
    val remoteDir: File,
) {

    val localProjectFile: ProjectFile<File> =
        FileSystemProjectFile(localDir)

    val remoteProjectFile: ProjectFile<File> =
        FileSystemProjectFile(remoteDir)

}
