package org.eazyportal.plugin.release.core.scm.model

import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.MAIN_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE

data class ScmConfig(
    val featureBranch: String,
    val releaseBranch: String,
    val remote: String
) {

    companion object {
        val GIT_FLOW = ScmConfig(FEATURE_BRANCH, MAIN_BRANCH, REMOTE)
        val TRUNK_BASED_FLOW = ScmConfig(MAIN_BRANCH, MAIN_BRANCH, REMOTE)
    }

}
