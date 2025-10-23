package org.eazyportal.plugin.gradle.release.core.project

import org.eazyportal.plugin.gradle.release.core.version.model.Version

interface ProjectActions<T: Any> {

    fun getVersion(): Version

    fun scmFilesToCommit(): Array<String>

    fun setVersion(version: Version)

}
