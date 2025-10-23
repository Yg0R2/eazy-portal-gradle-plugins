package org.eazyportal.plugin.gradle.release.core.scm.model

data class ScmConfig(
    val featureBranch: String,
    val releaseBranch: String,
    val remote: String
) {

    companion object {
        val GIT_FLOW = ScmConfig("dev", "main", "origin")
        val TRUNK_BASED_FLOW = ScmConfig("main", "main", "origin")
    }

}
