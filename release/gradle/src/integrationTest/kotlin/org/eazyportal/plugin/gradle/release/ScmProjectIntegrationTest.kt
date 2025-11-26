package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File

interface ScmProjectIntegrationTest {

    val scmUtils: ScmUtils

    val projectDir: File
    val remoteProjectDir: File

    fun getProjectVersion(projectDir: File, branch: String? = null): Version

    fun setProjectVersion(projectDir: File, version: Version)

}
