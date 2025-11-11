package org.eazyportal.plugin.release.core.project

import org.eazyportal.plugin.release.core.version.model.Version

interface ProjectActions<T: Any> {

    fun getVersion(): Version

    fun scmFilesToCommit(): Array<String>

    fun setVersion(version: Version)

}
